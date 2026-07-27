package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Router implements HttpHandler {
    @FunctionalInterface
    public interface RouteHandler {
        void handle(HttpExchange exchange) throws IOException;
    }

    private final Map<String, RouteHandler> routes = new HashMap<>();

    public void get(String path, RouteHandler handler) {
        add("GET", path, handler);
    }

    public void post(String path, RouteHandler handler) {
        add("POST", path, handler);
    }

    public void patch(String path, RouteHandler handler) {
        add("PATCH", path, handler);
    }

    public void delete(String path, RouteHandler handler) {
        add("DELETE", path, handler);
    }

    private void add(String method, String path, RouteHandler handler) {
        routes.put(routeKey(method, normalizePath(path)), handler);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JsonResponse.addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonResponse.noContent(exchange);
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        String path = normalizePath(exchange.getRequestURI().getPath());
        RouteHandler handler = routes.get(routeKey(method, path));

        if (handler == null) {
            JsonResponse.notFound(exchange, "Route not found: " + method + " " + path);
            return;
        }

        try {
            handler.handle(exchange);
        } catch (IllegalArgumentException ex) {
            JsonResponse.badRequest(exchange, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsonResponse.serverError(exchange, "Internal server error");
        }
    }

    private String routeKey(String method, String path) {
        return method + " " + path;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        String normalized = path.trim();

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}
