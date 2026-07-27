package security;

import config.AppConfig;
import http.JsonResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class JwtUtil {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private JwtUtil() {
    }

    public static String generateToken(long userId, String roleCode) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + (long) AppConfig.getJwtExpirationMinutes() * 60L;

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = "{"
                + "\"userId\":" + userId + ","
                + "\"role\":\"" + JsonResponse.escape(roleCode) + "\","
                + "\"iat\":" + issuedAt + ","
                + "\"exp\":" + expiresAt
                + "}";

        String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = sign(header + "." + payload);

        return header + "." + payload + "." + signature;
    }

    public static Optional<JwtPayload> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);

        if (!constantTimeEquals(expectedSignature, parts[2])) {
            return Optional.empty();
        }

        String payloadJson;
        try {
            payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        Map<String, String> claims = parseFlatJson(payloadJson);

        try {
            long userId = Long.parseLong(claims.getOrDefault("userId", "0"));
            String role = claims.get("role");
            long expiresAt = Long.parseLong(claims.getOrDefault("exp", "0"));

            if (userId <= 0 || role == null || role.isBlank()) {
                return Optional.empty();
            }

            if (Instant.now().getEpochSecond() >= expiresAt) {
                return Optional.empty();
            }

            return Optional.of(new JwtPayload(userId, role, expiresAt));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public static Optional<JwtPayload> validateBearerHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }

        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) {
            return Optional.empty();
        }

        return validateToken(authorizationHeader.substring(prefix.length()).trim());
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    AppConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(keySpec);
            return base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not sign JWT token", ex);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private static boolean constantTimeEquals(String first, String second) {
        if (first == null || second == null) {
            return false;
        }

        byte[] a = first.getBytes(StandardCharsets.UTF_8);
        byte[] b = second.getBytes(StandardCharsets.UTF_8);

        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }

        return result == 0;
    }

    private static Map<String, String> parseFlatJson(String json) {
        Map<String, String> result = new LinkedHashMap<>();

        if (json == null || json.isBlank()) {
            return result;
        }

        String body = json.trim();
        if (body.startsWith("{")) {
            body = body.substring(1);
        }
        if (body.endsWith("}")) {
            body = body.substring(0, body.length() - 1);
        }

        String[] pairs = body.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                continue;
            }

            String key = cleanJsonValue(keyValue[0]);
            String value = cleanJsonValue(keyValue[1]);
            result.put(key, value);
        }

        return result;
    }

    private static String cleanJsonValue(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = value.trim();

        if (cleaned.startsWith("\"")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public record JwtPayload(long userId, String roleCode, long expiresAt) {
    }
}
