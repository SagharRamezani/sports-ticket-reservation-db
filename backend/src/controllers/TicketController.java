package controllers;

import com.sun.net.httpserver.HttpExchange;
import http.JsonResponse;
import services.SearchService;
import services.TicketService;

import java.io.IOException;

public class TicketController {
    private final TicketService ticketService;
    private final SearchService searchService;

    public TicketController() {
        this.ticketService = new TicketService();
        this.searchService = new SearchService();
    }

    public void listTickets(HttpExchange exchange) throws IOException {
        String responseJson = ticketService.listTickets();
        JsonResponse.ok(exchange, responseJson);
    }

    public void searchTickets(HttpExchange exchange) throws IOException {
        String responseJson = searchService.searchTickets(exchange.getRequestURI().getRawQuery());
        JsonResponse.ok(exchange, responseJson);
    }

    public void getTicketDetail(HttpExchange exchange) throws IOException {
        long ticketId = extractLastPathLong(exchange, "ticketId");
        String responseJson = ticketService.getTicketDetail(ticketId);
        JsonResponse.ok(exchange, responseJson);
    }

    private long extractLastPathLong(HttpExchange exchange, String parameterName) {
        String path = exchange.getRequestURI().getPath();

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Missing path parameter: " + parameterName);
        }

        String[] parts = path.split("/");
        String lastPart = "";

        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i] != null && !parts[i].isBlank()) {
                lastPart = parts[i];
                break;
            }
        }

        if (lastPart.isBlank()) {
            throw new IllegalArgumentException("Missing path parameter: " + parameterName);
        }

        try {
            long value = Long.parseLong(lastPart);
            if (value <= 0) {
                throw new IllegalArgumentException("Invalid path parameter: " + parameterName);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid path parameter: " + parameterName);
        }
    }
}
