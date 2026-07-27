package controllers;

import com.sun.net.httpserver.HttpExchange;
import http.JsonResponse;
import http.RequestUtils;
import security.AuthMiddleware;
import security.AuthMiddleware.AuthUser;
import services.ReportService;

import java.io.IOException;
import java.util.Optional;

public class ReportController {
    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportService();
    }

    public void createReport(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        String requestBody = RequestUtils.readBody(exchange);
        String responseJson = reportService.createReport(authUser.get().userId(), requestBody);

        JsonResponse.created(exchange, responseJson);
    }

    public void getMyReports(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        String responseJson = reportService.getMyReports(authUser.get().userId());
        JsonResponse.ok(exchange, responseJson);
    }
}
