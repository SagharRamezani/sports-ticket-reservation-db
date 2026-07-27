package services;

import elastic.ElasticTicketSearch;
import http.JsonResponse;
import repositories.TicketRepository;
import repositories.TicketRepository.TicketResult;
import repositories.TicketRepository.TicketSearchFilter;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchService {
    private final TicketRepository ticketRepository;
    private final ElasticTicketSearch elasticTicketSearch;

    public SearchService() {
        this.ticketRepository = new TicketRepository();
        this.elasticTicketSearch = new ElasticTicketSearch();
    }

    public String searchTickets(String rawQuery) {
        Map<String, String> queryParams = parseQueryParams(rawQuery);

        TicketSearchFilter filter = new TicketSearchFilter(
                queryParams.get("sport"),
                queryParams.get("city"),
                queryParams.get("venue"),
                queryParams.get("team"),
                queryParams.get("category"),
                readBigDecimal(queryParams.get("min_price"), "min_price"),
                readBigDecimal(queryParams.get("max_price"), "max_price"),
                queryParams.get("match_date")
        );

        validateFilter(filter);

        SearchResult searchResult = searchWithElasticFallback(filter);

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,");
        json.append("\"searchEngine\":\"").append(JsonResponse.escape(searchResult.searchEngine())).append("\",");
        json.append("\"fallbackUsed\":").append(searchResult.fallbackUsed()).append(",");
        json.append("\"filters\":").append(buildFilterJson(filter)).append(",");
        json.append("\"data\":[");

        for (int i = 0; i < searchResult.tickets().size(); i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append(buildTicketJson(searchResult.tickets().get(i)));
        }

        json.append("],\"count\":").append(searchResult.tickets().size()).append("}");
        return json.toString();
    }

    private SearchResult searchWithElasticFallback(TicketSearchFilter filter) {
        try {
            if (elasticTicketSearch.isAvailable()) {
                List<Long> ticketIds = elasticTicketSearch.searchTicketIds(filter);

                if (!ticketIds.isEmpty()) {
                    List<TicketResult> tickets = ticketRepository.findTicketsByIds(ticketIds);
                    return new SearchResult(tickets, "elasticsearch", false);
                }

                return new SearchResult(List.of(), "elasticsearch", false);
            }
        } catch (RuntimeException ex) {
            // Elasticsearch is optional in phase 4.
            // If it is down, misconfigured, or returns an invalid response, SQL search must still work.
        }

        List<TicketResult> tickets = ticketRepository.searchTickets(filter);
        return new SearchResult(tickets, "sql", true);
    }

    private void validateFilter(TicketSearchFilter filter) {
        if (filter.minPrice() != null && filter.minPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("min_price cannot be negative");
        }

        if (filter.maxPrice() != null && filter.maxPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("max_price cannot be negative");
        }

        if (filter.minPrice() != null
                && filter.maxPrice() != null
                && filter.minPrice().compareTo(filter.maxPrice()) > 0) {
            throw new IllegalArgumentException("min_price cannot be greater than max_price");
        }

        if (filter.matchDate() != null
                && !filter.matchDate().isBlank()
                && !filter.matchDate().matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("match_date format must be YYYY-MM-DD");
        }
    }

    private BigDecimal readBigDecimal(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number");
        }
    }

    private String buildFilterJson(TicketSearchFilter filter) {
        return "{"
                + "\"sport\":\"" + JsonResponse.escape(filter.sport()) + "\","
                + "\"city\":\"" + JsonResponse.escape(filter.city()) + "\","
                + "\"venue\":\"" + JsonResponse.escape(filter.venue()) + "\","
                + "\"team\":\"" + JsonResponse.escape(filter.team()) + "\","
                + "\"category\":\"" + JsonResponse.escape(filter.category()) + "\","
                + "\"minPrice\":" + moneyOrNull(filter.minPrice()) + ","
                + "\"maxPrice\":" + moneyOrNull(filter.maxPrice()) + ","
                + "\"matchDate\":\"" + JsonResponse.escape(filter.matchDate()) + "\""
                + "}";
    }

    private String buildTicketJson(TicketResult ticket) {
        return "{"
                + "\"ticketId\":" + ticket.ticketId() + ","
                + "\"ticketStatus\":\"" + JsonResponse.escape(ticket.ticketStatus()) + "\","
                + "\"seatNumber\":\"" + JsonResponse.escape(ticket.seatNumber()) + "\","
                + "\"price\":" + money(ticket.price()) + ","
                + "\"category\":{"
                + "\"categoryId\":" + nullableLong(ticket.categoryId()) + ","
                + "\"categoryName\":\"" + JsonResponse.escape(ticket.categoryName()) + "\""
                + "},"
                + "\"match\":{"
                + "\"matchId\":" + ticket.matchId() + ","
                + "\"matchTitle\":\"" + JsonResponse.escape(ticket.matchTitle()) + "\","
                + "\"matchStartTime\":\"" + JsonResponse.escape(ticket.matchStartTime()) + "\","
                + "\"matchStatus\":\"" + JsonResponse.escape(ticket.matchStatus()) + "\""
                + "},"
                + "\"sport\":{"
                + "\"sportId\":" + nullableLong(ticket.sportId()) + ","
                + "\"sportName\":\"" + JsonResponse.escape(ticket.sportName()) + "\""
                + "},"
                + "\"venue\":{"
                + "\"venueId\":" + nullableLong(ticket.venueId()) + ","
                + "\"venueName\":\"" + JsonResponse.escape(ticket.venueName()) + "\","
                + "\"cityName\":\"" + JsonResponse.escape(ticket.cityName()) + "\""
                + "},"
                + "\"teams\":{"
                + "\"homeTeamName\":\"" + JsonResponse.escape(ticket.homeTeamName()) + "\","
                + "\"awayTeamName\":\"" + JsonResponse.escape(ticket.awayTeamName()) + "\""
                + "}"
                + "}";
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }

        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String moneyOrNull(BigDecimal value) {
        if (value == null) {
            return "null";
        }

        return money(value);
    }

    private String nullableLong(Long value) {
        return value == null ? "null" : String.valueOf(value);
    }

    private Map<String, String> parseQueryParams(String rawQuery) {
        Map<String, String> params = new LinkedHashMap<>();

        if (rawQuery == null || rawQuery.isBlank()) {
            return params;
        }

        String[] pairs = rawQuery.split("&");

        for (String pair : pairs) {
            if (pair == null || pair.isBlank()) {
                continue;
            }

            String[] keyValue = pair.split("=", 2);
            String key = decode(keyValue[0]).trim();

            if (key.isBlank()) {
                continue;
            }

            String value = keyValue.length > 1 ? decode(keyValue[1]).trim() : "";

            if (!value.isBlank()) {
                params.put(key, value);
            }
        }

        return params;
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("UTF-8 is not supported", ex);
        }
    }

    private record SearchResult(
            List<TicketResult> tickets,
            String searchEngine,
            boolean fallbackUsed
    ) {
    }
}
