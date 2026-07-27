package controllers;

import com.sun.net.httpserver.HttpExchange;
import http.JsonResponse;
import http.RequestUtils;
import security.AuthMiddleware;
import security.AuthMiddleware.AuthUser;
import services.ReservationService;

import java.io.IOException;
import java.util.Optional;

public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController() {
        this.reservationService = new ReservationService();
    }

    public void reserveTicket(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        long ticketId = RequestUtils.pathLong(exchange, 2, "ticketId");
        String responseJson = reservationService.reserveTicket(authUser.get().userId(), ticketId);

        JsonResponse.created(exchange, responseJson);
    }

    public void getCancellationPenalty(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        long reservationId = RequestUtils.pathLong(exchange, 2, "reservationId");
        String responseJson = reservationService.getCancellationPenalty(
                authUser.get().userId(),
                authUser.get().roleCode(),
                reservationId
        );

        JsonResponse.ok(exchange, responseJson);
    }

    public void cancelReservation(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        long reservationId = RequestUtils.pathLong(exchange, 2, "reservationId");
        String requestBody = RequestUtils.readBody(exchange);

        String responseJson = reservationService.cancelReservation(
                authUser.get().userId(),
                authUser.get().roleCode(),
                reservationId,
                requestBody
        );

        JsonResponse.ok(exchange, responseJson);
    }

    public void cleanupExpiredReservations(HttpExchange exchange) throws IOException {
        Optional<AuthUser> supportUser = AuthMiddleware.requireSupport(exchange);
        if (supportUser.isEmpty()) {
            return;
        }

        String responseJson = reservationService.cleanupExpiredReservations();
        JsonResponse.ok(exchange, responseJson);
    }
}
