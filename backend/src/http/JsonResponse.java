package http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class JsonResponse {
    private JsonResponse() {
    }

    public static void ok(HttpExchange exchange, String jsonBody) throws IOException {
        send(exchange, 200, jsonBody);
    }

    public static void created(HttpExchange exchange, String jsonBody) throws IOException {
        send(exchange, 201, jsonBody);
    }

    public static void noContent(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    public static void badRequest(HttpExchange exchange, String message) throws IOException {
        send(exchange, 400, errorJson("BAD_REQUEST", message));
    }

    public static void unauthorized(HttpExchange exchange, String message) throws IOException {
        send(exchange, 401, errorJson("UNAUTHORIZED", message));
    }

    public static void forbidden(HttpExchange exchange, String message) throws IOException {
        send(exchange, 403, errorJson("FORBIDDEN", message));
    }

    public static void notFound(HttpExchange exchange, String message) throws IOException {
        send(exchange, 404, errorJson("NOT_FOUND", message));
    }

    public static void conflict(HttpExchange exchange, String message) throws IOException {
        send(exchange, 409, errorJson("CONFLICT", message));
    }

    public static void serverError(HttpExchange exchange, String message) throws IOException {
        send(exchange, 500, errorJson("SERVER_ERROR", message));
    }

    public static void send(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        addCorsHeaders(exchange);

        String responseBody = jsonBody == null || jsonBody.isBlank() ? "{}" : jsonBody;
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    public static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PATCH, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static String errorJson(String code, String message) {
        return "{"
                + "\"success\":false,"
                + "\"error\":{"
                + "\"code\":\"" + escape(code) + "\","
                + "\"message\":\"" + escape(message) + "\""
                + "}"
                + "}";
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
