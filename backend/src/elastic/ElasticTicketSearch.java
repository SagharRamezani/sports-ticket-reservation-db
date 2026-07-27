package elastic;

import repositories.TicketRepository.TicketResult;
import repositories.TicketRepository.TicketSearchFilter;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ElasticTicketSearch {
    private static final String DEFAULT_ELASTIC_URL = "http://localhost:9200";
    private static final String DEFAULT_INDEX_NAME = "tickets";
    private static final int DEFAULT_TIMEOUT_SECONDS = 2;

    private final HttpClient httpClient;
    private final String elasticBaseUrl;
    private final String indexName;
    private final int timeoutSeconds;

    public ElasticTicketSearch() {
        this(
                readEnv("ELASTIC_URL", DEFAULT_ELASTIC_URL),
                readEnv("ELASTIC_TICKET_INDEX", DEFAULT_INDEX_NAME),
                readIntEnv("ELASTIC_TIMEOUT_SECONDS", DEFAULT_TIMEOUT_SECONDS)
        );
    }

    public ElasticTicketSearch(String elasticBaseUrl, String indexName, int timeoutSeconds) {
        this.elasticBaseUrl = normalizeBaseUrl(elasticBaseUrl);
        this.indexName = (indexName == null || indexName.isBlank()) ? DEFAULT_INDEX_NAME : indexName.trim();
        this.timeoutSeconds = timeoutSeconds <= 0 ? DEFAULT_TIMEOUT_SECONDS : timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(this.timeoutSeconds))
                .build();
    }

    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(elasticBaseUrl))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (IOException | InterruptedException | IllegalArgumentException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    public String searchTicketsRaw(TicketSearchFilter filter) {
        String requestBody = buildSearchRequest(filter);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(elasticBaseUrl + "/" + url(indexName) + "/_search"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Elasticsearch search failed with status " + response.statusCode());
            }

            return response.body();
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Elasticsearch is not available", ex);
        }
    }

    public List<Long> searchTicketIds(TicketSearchFilter filter) {
        String rawResponse = searchTicketsRaw(filter);
        return extractTicketIds(rawResponse);
    }

    public String suggestionsRaw(String query, int size) {
        String safeQuery = query == null ? "" : query.trim();

        if (safeQuery.isBlank()) {
            return "{\"hits\":{\"hits\":[]}}";
        }

        int safeSize = size <= 0 ? 5 : Math.min(size, 20);

        String body = "{"
                + "\"size\":" + safeSize + ","
                + "\"_source\":[\"ticket_id\",\"match_title\",\"sport\",\"home_team\",\"away_team\",\"venue\",\"city\",\"category\"],"
                + "\"query\":{"
                + "\"multi_match\":{"
                + "\"query\":\"" + json(safeQuery) + "\","
                + "\"type\":\"bool_prefix\","
                + "\"fields\":[\"suggest\",\"suggest._2gram\",\"suggest._3gram\"]"
                + "}"
                + "}"
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(elasticBaseUrl + "/" + url(indexName) + "/_search"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Elasticsearch suggestions failed with status " + response.statusCode());
            }

            return response.body();
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Elasticsearch is not available", ex);
        }
    }

    public void indexTicket(TicketResult ticket) {
        if (ticket == null || ticket.ticketId() <= 0) {
            throw new IllegalArgumentException("Valid ticket is required");
        }

        String documentJson = buildTicketDocument(ticket);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(elasticBaseUrl + "/" + url(indexName) + "/_doc/" + ticket.ticketId()))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(documentJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Could not index ticket in Elasticsearch");
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Elasticsearch is not available", ex);
        }
    }

    public void deleteTicket(long ticketId) {
        if (ticketId <= 0) {
            throw new IllegalArgumentException("Invalid ticketId");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(elasticBaseUrl + "/" + url(indexName) + "/_doc/" + ticketId))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 404 && (response.statusCode() < 200 || response.statusCode() >= 300)) {
                throw new IllegalStateException("Could not delete ticket from Elasticsearch");
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Elasticsearch is not available", ex);
        }
    }

    private String buildSearchRequest(TicketSearchFilter filter) {
        StringBuilder must = new StringBuilder();
        StringBuilder filters = new StringBuilder();

        String textQuery = firstNonBlank(
                filter == null ? null : filter.team(),
                filter == null ? null : filter.venue(),
                filter == null ? null : filter.city(),
                filter == null ? null : filter.sport(),
                filter == null ? null : filter.category()
        );

        if (textQuery != null) {
            addClause(must, "{"
                    + "\"multi_match\":{"
                    + "\"query\":\"" + json(textQuery) + "\","
                    + "\"fields\":[\"search_text\",\"match_title\",\"sport\",\"home_team\",\"away_team\",\"venue\",\"city\",\"category\",\"features\"]"
                    + "}"
                    + "}");
        }

        if (filter != null) {
            addTermFilter(filters, "sport.keyword", filter.sport());
            addTermFilter(filters, "city.keyword", filter.city());
            addTermFilter(filters, "venue.keyword", filter.venue());
            addTermFilter(filters, "category.keyword", filter.category());

            if (filter.team() != null && !filter.team().isBlank()) {
                addClause(filters, "{"
                        + "\"bool\":{"
                        + "\"should\":["
                        + "{\"match\":{\"home_team\":\"" + json(filter.team()) + "\"}},"
                        + "{\"match\":{\"away_team\":\"" + json(filter.team()) + "\"}}"
                        + "],"
                        + "\"minimum_should_match\":1"
                        + "}"
                        + "}");
            }

            addPriceRangeFilter(filters, filter.minPrice(), filter.maxPrice());
            addMatchDateFilter(filters, filter.matchDate());
        }

        addTermFilter(filters, "ticket_status.keyword", "AVAILABLE");

        String mustJson = must.isEmpty() ? "\"must\":[{\"match_all\":{}}]" : "\"must\":[" + must + "]";
        String filterJson = filters.isEmpty() ? "\"filter\":[]" : "\"filter\":[" + filters + "]";

        return "{"
                + "\"size\":100,"
                + "\"query\":{"
                + "\"bool\":{"
                + mustJson + ","
                + filterJson
                + "}"
                + "},"
                + "\"sort\":["
                + "{\"match_time\":{\"order\":\"asc\"}},"
                + "{\"price\":{\"order\":\"asc\"}}"
                + "]"
                + "}";
    }

    private void addTermFilter(StringBuilder filters, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        addClause(filters, "{\"term\":{\"" + json(fieldName) + "\":\"" + json(value.trim()) + "\"}}");
    }

    private void addPriceRangeFilter(StringBuilder filters, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return;
        }

        StringBuilder range = new StringBuilder();
        range.append("{\"range\":{\"price\":{");

        boolean hasPrevious = false;

        if (minPrice != null) {
            range.append("\"gte\":").append(minPrice.toPlainString());
            hasPrevious = true;
        }

        if (maxPrice != null) {
            if (hasPrevious) {
                range.append(",");
            }

            range.append("\"lte\":").append(maxPrice.toPlainString());
        }

        range.append("}}}");
        addClause(filters, range.toString());
    }

    private void addMatchDateFilter(StringBuilder filters, String matchDate) {
        if (matchDate == null || matchDate.isBlank()) {
            return;
        }

        String date = matchDate.trim();

        addClause(filters, "{"
                + "\"range\":{"
                + "\"match_time\":{"
                + "\"gte\":\"" + json(date) + "T00:00:00\","
                + "\"lt\":\"" + json(date) + "T23:59:59\""
                + "}"
                + "}"
                + "}");
    }

    private void addClause(StringBuilder builder, String clause) {
        if (!builder.isEmpty()) {
            builder.append(",");
        }

        builder.append(clause);
    }

    private List<Long> extractTicketIds(String rawResponse) {
        List<Long> ids = new ArrayList<>();

        if (rawResponse == null || rawResponse.isBlank()) {
            return ids;
        }

        String marker = "\"ticket_id\"";
        int searchFrom = 0;

        while (searchFrom < rawResponse.length()) {
            int index = rawResponse.indexOf(marker, searchFrom);

            if (index < 0) {
                break;
            }

            int colon = rawResponse.indexOf(":", index + marker.length());

            if (colon < 0) {
                break;
            }

            int start = colon + 1;
            while (start < rawResponse.length() && Character.isWhitespace(rawResponse.charAt(start))) {
                start++;
            }

            int end = start;
            while (end < rawResponse.length() && Character.isDigit(rawResponse.charAt(end))) {
                end++;
            }

            if (end > start) {
                try {
                    ids.add(Long.parseLong(rawResponse.substring(start, end)));
                } catch (NumberFormatException ignored) {
                    // Ignore malformed ticket id and continue reading the response.
                }
            }

            searchFrom = end;
        }

        return ids;
    }

    private String buildTicketDocument(TicketResult ticket) {
        String searchText = String.join(" ",
                safe(ticket.sportName()),
                safe(ticket.homeTeamName()),
                safe(ticket.awayTeamName()),
                safe(ticket.matchTitle()),
                safe(ticket.venueName()),
                safe(ticket.cityName()),
                safe(ticket.categoryName()),
                safe(ticket.seatNumber())
        ).trim();

        return "{"
                + "\"ticket_id\":" + ticket.ticketId() + ","
                + "\"match_id\":" + ticket.matchId() + ","
                + "\"sport\":\"" + json(ticket.sportName()) + "\","
                + "\"home_team\":\"" + json(ticket.homeTeamName()) + "\","
                + "\"away_team\":\"" + json(ticket.awayTeamName()) + "\","
                + "\"match_title\":\"" + json(ticket.matchTitle()) + "\","
                + "\"venue\":\"" + json(ticket.venueName()) + "\","
                + "\"city\":\"" + json(ticket.cityName()) + "\","
                + "\"match_time\":\"" + json(ticket.matchStartTime()) + "\","
                + "\"category\":\"" + json(ticket.categoryName()) + "\","
                + "\"section_name\":\"\","
                + "\"row_number\":\"\","
                + "\"seat_number\":\"" + json(ticket.seatNumber()) + "\","
                + "\"price\":" + money(ticket.price()) + ","
                + "\"available_capacity\":1,"
                + "\"ticket_status\":\"" + json(ticket.ticketStatus()) + "\","
                + "\"features\":[],"
                + "\"suggest\":\"" + json(searchText) + "\","
                + "\"search_text\":\"" + json(searchText) + "\","
                + "\"indexed_at\":\"" + java.time.Instant.now() + "\""
                + "}";
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0";
        }

        return value.toPlainString();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    private static String normalizeBaseUrl(String url) {
        String safeUrl = (url == null || url.isBlank()) ? DEFAULT_ELASTIC_URL : url.trim();

        while (safeUrl.endsWith("/")) {
            safeUrl = safeUrl.substring(0, safeUrl.length() - 1);
        }

        return safeUrl;
    }

    private static String readEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static int readIntEnv(String name, int defaultValue) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String json(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
