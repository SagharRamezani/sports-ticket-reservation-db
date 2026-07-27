package http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class RequestUtils {
    private RequestUtils() {
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static Map<String, String> queryParams(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        Map<String, String> params = new HashMap<>();

        if (rawQuery == null || rawQuery.isBlank()) {
            return params;
        }

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }

            String[] keyValue = pair.split("=", 2);
            String key = decode(keyValue[0]);
            String value = keyValue.length > 1 ? decode(keyValue[1]) : "";

            params.put(key, value);
        }

        return params;
    }

    public static String getQueryParam(HttpExchange exchange, String name) {
        return queryParams(exchange).get(name);
    }

    public static String pathSegment(HttpExchange exchange, int index) {
        String path = exchange.getRequestURI().getPath();
        String[] segments = path.split("/");

        int currentIndex = 0;
        for (String segment : segments) {
            if (segment.isBlank()) {
                continue;
            }

            if (currentIndex == index) {
                return decode(segment);
            }

            currentIndex++;
        }

        return null;
    }

    public static Long pathLong(HttpExchange exchange, int index, String fieldName) {
        String value = pathSegment(exchange, index);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required in path");
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a number");
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
