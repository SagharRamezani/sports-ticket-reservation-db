package controllers;

import com.sun.net.httpserver.HttpExchange;
import http.JsonResponse;
import http.RequestUtils;
import security.AuthMiddleware;
import security.AuthMiddleware.AuthUser;
import services.UserService;

import java.io.IOException;
import java.util.Optional;

public class UserController {
    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public void getMyProfile(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        String responseJson = userService.getMyProfile(authUser.get().userId());
        JsonResponse.ok(exchange, responseJson);
    }

    public void updateMyProfile(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        String requestBody = RequestUtils.readBody(exchange);
        String responseJson = userService.updateMyProfile(authUser.get().userId(), requestBody);

        JsonResponse.ok(exchange, responseJson);
    }

    public void getMyBookings(HttpExchange exchange) throws IOException {
        Optional<AuthUser> authUser = AuthMiddleware.requireAuth(exchange);
        if (authUser.isEmpty()) {
            return;
        }

        String responseJson = userService.getMyBookings(authUser.get().userId());
        JsonResponse.ok(exchange, responseJson);
    }
}
