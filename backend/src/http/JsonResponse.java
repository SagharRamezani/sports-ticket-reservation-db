package http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import config.AppConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class JsonResponse {
    private JsonResponse() {
    }

    public static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();

        String allowedOrigin = AppConfig.isFrontendDevMode() ? "*" : "http://localhost:5500";

        headers.set("Access-Control-Allow-Origin", allowedOrigin);
        headers.set("Access-Control-Allow-Methods", "GET, POST, PATCH, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
        headers.set("Access-Control-Max-Age", "86400");
    }

    public static void ok(HttpExchange exchange, String json) throws IOException {
        sendJson(exchange, 200, json);
    }

    public static void created(HttpExchange exchange, String json) throws IOException {
        sendJson(exchange, 201, json);
    }

    public static void noContent(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    public static void badRequest(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 400, errorJson("BAD_REQUEST", message));
    }

    public static void unauthorized(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 401, errorJson("UNAUTHORIZED", message));
    }

    public static void forbidden(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 403, errorJson("FORBIDDEN", message));
    }

    public static void notFound(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 404, errorJson("NOT_FOUND", message));
    }

    public static void conflict(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 409, errorJson("CONFLICT", message));
    }

    public static void serverError(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 500, errorJson("INTERNAL_SERVER_ERROR", message));
    }

    public static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        addCorsHeaders(exchange);

        String responseBody = normalizeJson(json);
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);

        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");

        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    public static String successJson(String message) {
        return "{"
                + "\"success\":true,"
                + "\"message\":\"" + escape(message) + "\""
                + "}";
    }

    public static String errorJson(String code, String message) {
        return "{"
                + "\"success\":false,"
                + "\"error\":{"
                + "\"code\":\"" + escape(code) + "\","
                + "\"message\":\"" + escape(message) + "\""
                + "}"
                + "}";
    }

    private static String normalizeJson(String json) {
        if (json == null || json.isBlank()) {
            return "{}";
        }

        String trimmed = json.trim();

        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return trimmed;
        }

        return "{"
                + "\"success\":true,"
                + "\"message\":\"" + escape(trimmed) + "\""
                + "}";
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
