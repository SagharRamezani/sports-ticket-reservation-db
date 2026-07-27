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
        String response = ticketService.listTickets();
        JsonResponse.ok(exchange, response);
    }

    public void searchTickets(HttpExchange exchange) throws IOException {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        String response = searchService.searchTickets(rawQuery);
        JsonResponse.ok(exchange, response);
    }

    public void getTicketDetail(HttpExchange exchange) throws IOException {
        long ticketId = readIdFromPath(exchange, "/api/tickets/");
        String response = ticketService.getTicketDetail(ticketId);
        JsonResponse.ok(exchange, response);
    }

    public void listCities(HttpExchange exchange) throws IOException {
        String response = ticketService.listCities();
        JsonResponse.ok(exchange, response);
    }

    public void listVenues(HttpExchange exchange) throws IOException {
        String response = ticketService.listVenues();
        JsonResponse.ok(exchange, response);
    }

    private long readIdFromPath(HttpExchange exchange, String prefix) {
        String path = exchange.getRequestURI().getPath();

        if (path == null || !path.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid ticket path");
        }

        String value = path.substring(prefix.length()).trim();

        int slashIndex = value.indexOf("/");

        if (slashIndex >= 0) {
            value = value.substring(0, slashIndex);
        }

        if (value.isBlank()) {
            throw new IllegalArgumentException("Ticket id is required");
        }

        try {
            long id = Long.parseLong(value);

            if (id <= 0) {
                throw new IllegalArgumentException("Ticket id must be positive");
            }

            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ticket id must be a valid number");
        }
    }
}
