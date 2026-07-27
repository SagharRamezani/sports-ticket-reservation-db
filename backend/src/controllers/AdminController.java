package controllers;

import com.sun.net.httpserver.HttpExchange;
import http.JsonResponse;
import http.RequestUtils;
import security.AuthMiddleware;
import security.AuthMiddleware.AuthUser;
import services.AdminService;

import java.io.IOException;
import java.util.Optional;

public class AdminController {
    private final AdminService adminService;

    public AdminController() {
        this.adminService = new AdminService();
    }

    public void getReports(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        String responseJson = adminService.getReports(authUser.get().roleCode());
        JsonResponse.ok(exchange, responseJson);
    }

    public void updateReport(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        long reportId = extractPathLong(exchange, "reports", "reportId");
        String requestBody = RequestUtils.readBody(exchange);

        String responseJson = adminService.updateReport(authUser.get().roleCode(), reportId, requestBody);
        JsonResponse.ok(exchange, responseJson);
    }

    public void getSuspiciousReservations(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        String responseJson = adminService.getSuspiciousReservations(authUser.get().roleCode());
        JsonResponse.ok(exchange, responseJson);
    }

    public void updateReservationStatus(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        long reservationId = extractPathLong(exchange, "reservations", "reservationId");
        String requestBody = RequestUtils.readBody(exchange);

        String responseJson = adminService.updateReservationStatus(authUser.get().roleCode(), reservationId, requestBody);
        JsonResponse.ok(exchange, responseJson);
    }

    private long extractPathLong(HttpExchange exchange, String marker, String parameterName) {
        String path = exchange.getRequestURI().getPath();

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Missing path parameter: " + parameterName);
        }

        String[] parts = path.split("/");

        for (int i = 0; i < parts.length - 1; i++) {
            if (marker.equals(parts[i])) {
                return parsePositiveLong(parts[i + 1], parameterName);
            }
        }

        throw new IllegalArgumentException("Missing path parameter: " + parameterName);
    }

    private long parsePositiveLong(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing path parameter: " + parameterName);
        }

        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException("Invalid path parameter: " + parameterName);
            }

            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid path parameter: " + parameterName);
        }
    }
}
