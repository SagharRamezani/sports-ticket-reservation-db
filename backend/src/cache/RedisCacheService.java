package cache;

import services.CacheService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RedisCacheService implements CacheService {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6379;
    private static final int DEFAULT_TIMEOUT_MILLIS = 700;
    private static final String KEY_PREFIX = "sports_ticket:";

    private final String host;
    private final int port;
    private final int timeoutMillis;
    private final MemoryCacheService fallbackCache;
    private volatile boolean enabled = true;
    private volatile boolean redisAvailable;

    public RedisCacheService(String host, int port) {
        this(host, port, DEFAULT_TIMEOUT_MILLIS);
    }

    public RedisCacheService(String host, int port, int timeoutMillis) {
        this.host = host == null || host.isBlank() ? DEFAULT_HOST : host.trim();
        this.port = port <= 0 ? DEFAULT_PORT : port;
        this.timeoutMillis = timeoutMillis <= 0 ? DEFAULT_TIMEOUT_MILLIS : timeoutMillis;
        this.fallbackCache = new MemoryCacheService();
        this.redisAvailable = pingRedis();
    }

    public static RedisCacheService fromEnvironment() {
        String host = valueOrDefault(System.getenv("REDIS_HOST"), DEFAULT_HOST);
        int port = parseIntOrDefault(System.getenv("REDIS_PORT"), DEFAULT_PORT);
        int timeout = parseIntOrDefault(System.getenv("REDIS_TIMEOUT_MILLIS"), DEFAULT_TIMEOUT_MILLIS);

        RedisCacheService redisCacheService = new RedisCacheService(host, port, timeout);

        String redisEnabled = System.getenv("REDIS_ENABLED");
        if (redisEnabled != null && redisEnabled.equalsIgnoreCase("false")) {
            redisCacheService.setEnabled(false);
        }

        return redisCacheService;
    }

    public boolean isRedisAvailable() {
        if (!enabled) {
            return false;
        }

        redisAvailable = pingRedis();
        return redisAvailable;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        fallbackCache.setEnabled(enabled);

        if (!enabled) {
            clear();
        }
    }

    @Override
    public void put(String key, String value, long ttlSeconds) {
        if (!enabled || key == null || key.isBlank() || value == null) {
            return;
        }

        long safeTtlSeconds = ttlSeconds <= 0 ? 300L : ttlSeconds;

        if (isRedisAvailable()) {
            try {
                sendCommand(
                        "SETEX",
                        normalizeKey(key),
                        String.valueOf(safeTtlSeconds),
                        value
                );
                return;
            } catch (IOException ex) {
                redisAvailable = false;
            }
        }

        fallbackCache.put(key, value, safeTtlSeconds);
    }

    @Override
    public Optional<String> get(String key) {
        if (!enabled || key == null || key.isBlank()) {
            return Optional.empty();
        }

        if (isRedisAvailable()) {
            try {
                RedisReply reply = sendCommand("GET", normalizeKey(key));

                if (reply == null || reply.value() == null) {
                    return Optional.empty();
                }

                return Optional.of(reply.value());
            } catch (IOException ex) {
                redisAvailable = false;
            }
        }

        return fallbackCache.get(key);
    }

    @Override
    public void remove(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        if (isRedisAvailable()) {
            try {
                sendCommand("DEL", normalizeKey(key));
            } catch (IOException ex) {
                redisAvailable = false;
            }
        }

        fallbackCache.remove(key);
    }

    @Override
    public void clear() {
        fallbackCache.clear();
    }

    private boolean pingRedis() {
        if (!enabled) {
            return false;
        }

        try {
            RedisReply reply = sendCommand("PING");
            return reply != null && "PONG".equalsIgnoreCase(reply.value());
        } catch (IOException ex) {
            return false;
        }
    }

    private RedisReply sendCommand(String... commandParts) throws IOException {
        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(timeoutMillis);

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
            );
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                 )) {
                writer.write(buildRespCommand(commandParts));
                writer.flush();

                return readReply(reader);
            }
        }
    }

    private String buildRespCommand(String... commandParts) {
        StringBuilder command = new StringBuilder();
        command.append("*").append(commandParts.length).append("\r\n");

        for (String part : commandParts) {
            String safePart = part == null ? "" : part;
            byte[] bytes = safePart.getBytes(StandardCharsets.UTF_8);

            command.append("$").append(bytes.length).append("\r\n");
            command.append(safePart).append("\r\n");
        }

        return command.toString();
    }

    private RedisReply readReply(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine();

        if (firstLine == null || firstLine.isEmpty()) {
            return new RedisReply(null);
        }

        char type = firstLine.charAt(0);
        String payload = firstLine.length() > 1 ? firstLine.substring(1) : "";

        if (type == '+') {
            return new RedisReply(payload);
        }

        if (type == '-') {
            throw new IOException("Redis error: " + payload);
        }

        if (type == ':') {
            return new RedisReply(payload);
        }

        if (type == '$') {
            int length = Integer.parseInt(payload);

            if (length < 0) {
                return new RedisReply(null);
            }

            char[] buffer = new char[length];
            int read = reader.read(buffer, 0, length);

            if (read < length) {
                throw new IOException("Redis bulk response was incomplete");
            }

            reader.readLine();
            return new RedisReply(new String(buffer));
        }

        return new RedisReply(payload);
    }

    private String normalizeKey(String key) {
        return KEY_PREFIX + key.trim();
    }

    private static String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private record RedisReply(String value) {
    }
}
