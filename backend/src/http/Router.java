package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Router implements HttpHandler {
    @FunctionalInterface
    public interface RouteHandler {
        void handle(HttpExchange exchange) throws IOException;
    }

    private final Map<String, RouteHandler> routes = new LinkedHashMap<>();

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

        RouteHandler handler = findHandler(method, path);

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

    private RouteHandler findHandler(String method, String path) {
        RouteHandler exactHandler = routes.get(routeKey(method, path));

        if (exactHandler != null) {
            return exactHandler;
        }

        for (Map.Entry<String, RouteHandler> route : routes.entrySet()) {
            String routeKey = route.getKey();

            if (!routeKey.startsWith(method + " ")) {
                continue;
            }

            String routePath = routeKey.substring(method.length() + 1);

            if (matches(routePath, path)) {
                return route.getValue();
            }
        }

        return null;
    }

    private boolean matches(String routePath, String requestPath) {
        String[] routeParts = routePath.split("/");
        String[] requestParts = requestPath.split("/");

        if (routeParts.length != requestParts.length) {
            return false;
        }

        for (int i = 0; i < routeParts.length; i++) {
            String routePart = routeParts[i];
            String requestPart = requestParts[i];

            if (routePart.startsWith("{") && routePart.endsWith("}")) {
                if (requestPart == null || requestPart.isBlank()) {
                    return false;
                }

                continue;
            }

            if (!routePart.equals(requestPart)) {
                return false;
            }
        }

        return true;
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