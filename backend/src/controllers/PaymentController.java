package controllers;

import com.sun.net.httpserver.HttpExchange;
import http.JsonResponse;
import http.RequestUtils;
import security.AuthMiddleware;
import security.AuthMiddleware.AuthUser;
import services.PaymentService;

import java.io.IOException;
import java.util.Optional;

public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController() {
        this.paymentService = new PaymentService();
    }

    public void payReservation(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        long reservationId = RequestUtils.pathLong(exchange, 2, "reservationId");
        String requestBody = RequestUtils.readBody(exchange);

        String responseJson = paymentService.payReservation(
                authUser.get().userId(),
                reservationId,
                requestBody
        );

        JsonResponse.created(exchange, responseJson);
    }

    public void listPaymentMethods(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        String responseJson = paymentService.listActivePaymentMethods();
        JsonResponse.ok(exchange, responseJson);
    }
}
