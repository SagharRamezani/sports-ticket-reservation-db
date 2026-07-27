package security;

import com.sun.net.httpserver.HttpExchange;
import http.JsonResponse;

import java.io.IOException;
import java.util.Optional;

public final class AuthMiddleware {
    private AuthMiddleware() {
    }

    public static Optional<AuthUser> getAuthenticatedUser(HttpExchange exchange) {
        String authorizationHeader = exchange.getRequestHeaders().getFirst("Authorization");

        return JwtUtil.validateBearerHeader(authorizationHeader)
                .map(payload -> new AuthUser(payload.userId(), payload.roleCode()));
    }

    public static Optional<AuthUser> requireAuth(HttpExchange exchange) throws IOException {
        Optional<AuthUser> user = getAuthenticatedUser(exchange);

        if (user.isEmpty()) {
            JsonResponse.unauthorized(exchange, "Authentication token is required or invalid");
        }

        return user;
    }

    public static Optional<AuthUser> requireRole(HttpExchange exchange, String requiredRole) throws IOException {
        Optional<AuthUser> user = requireAuth(exchange);

        if (user.isEmpty()) {
            return Optional.empty();
        }

        if (!user.get().hasRole(requiredRole)) {
            JsonResponse.forbidden(exchange, "Required role: " + requiredRole);
            return Optional.empty();
        }

        return user;
    }

    public static Optional<AuthUser> requireSupport(HttpExchange exchange) throws IOException {
        return requireRole(exchange, "SUPPORT");
    }

    public record AuthUser(long userId, String roleCode) {
        public boolean hasRole(String expectedRole) {
            return roleCode != null && roleCode.equalsIgnoreCase(expectedRole);
        }
    }
}
