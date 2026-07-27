package controllers;

import com.sun.net.httpserver.HttpExchange;
import http.JsonResponse;
import http.RequestUtils;
import services.AuthService;

import java.io.IOException;

public class AuthController {
    private final AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    public void signup(HttpExchange exchange) throws IOException {
        String requestBody = RequestUtils.readBody(exchange);
        String responseJson = authService.signup(requestBody);
        JsonResponse.created(exchange, responseJson);
    }

    public void login(HttpExchange exchange) throws IOException {
        String requestBody = RequestUtils.readBody(exchange);
        String responseJson = authService.login(requestBody);
        JsonResponse.ok(exchange, responseJson);
    }
}
