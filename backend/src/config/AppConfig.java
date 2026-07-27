package config;

public final class AppConfig {
    private static final String DEFAULT_SERVER_PORT = "8080";

    private static final String DEFAULT_DB_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/sports_ticket_reservation";
    private static final String DEFAULT_DB_USER = "postgres";
    private static final String DEFAULT_DB_PASSWORD = "postgres";
    private static final String DEFAULT_DB_LOGIN_TIMEOUT_SECONDS = "10";

    private static final String DEFAULT_JWT_SECRET = "sports-ticket-reservation-secret-key-change-in-production";
    private static final String DEFAULT_JWT_EXPIRATION_MINUTES = "1440";

    private static final String DEFAULT_OTP_TTL_MINUTES = "5";
    private static final String DEFAULT_RESERVATION_TTL_MINUTES = "15";

    private static final String DEFAULT_ELASTIC_URL = "http://localhost:9200";
    private static final String DEFAULT_ELASTIC_TICKET_INDEX = "tickets";
    private static final String DEFAULT_ELASTIC_TIMEOUT_SECONDS = "3";

    private AppConfig() {
    }

    public static int getServerPort() {
        return getInt("SERVER_PORT", DEFAULT_SERVER_PORT);
    }

    public static String getDbDriverClassName() {
        return get("DB_DRIVER_CLASS_NAME", DEFAULT_DB_DRIVER_CLASS_NAME);
    }

    public static String getDbUrl() {
        return get("DB_URL", DEFAULT_DB_URL);
    }

    public static String getDbUser() {
        return get("DB_USER", DEFAULT_DB_USER);
    }

    public static String getDbPassword() {
        return get("DB_PASSWORD", DEFAULT_DB_PASSWORD);
    }

    public static int getDbLoginTimeoutSeconds() {
        return getInt("DB_LOGIN_TIMEOUT_SECONDS", DEFAULT_DB_LOGIN_TIMEOUT_SECONDS);
    }

    public static String getJwtSecret() {
        return get("JWT_SECRET", DEFAULT_JWT_SECRET);
    }

    public static int getJwtExpirationMinutes() {
        return getInt("JWT_EXPIRATION_MINUTES", DEFAULT_JWT_EXPIRATION_MINUTES);
    }

    public static int getOtpTtlMinutes() {
        return getInt("OTP_TTL_MINUTES", DEFAULT_OTP_TTL_MINUTES);
    }

    public static int getReservationTtlMinutes() {
        return getInt("RESERVATION_TTL_MINUTES", DEFAULT_RESERVATION_TTL_MINUTES);
    }

    public static String getElasticUrl() {
        return removeTrailingSlash(get("ELASTIC_URL", DEFAULT_ELASTIC_URL));
    }

    public static String getElasticTicketIndex() {
        return get("ELASTIC_TICKET_INDEX", DEFAULT_ELASTIC_TICKET_INDEX);
    }

    public static int getElasticTimeoutSeconds() {
        return getInt("ELASTIC_TIMEOUT_SECONDS", DEFAULT_ELASTIC_TIMEOUT_SECONDS);
    }

    public static boolean isElasticEnabled() {
        return getBoolean("ELASTIC_ENABLED", "true");
    }

    public static boolean isFrontendDevMode() {
        return getBoolean("FRONTEND_DEV_MODE", "true");
    }

    private static String get(String key, String defaultValue) {
        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    private static int getInt(String key, String defaultValue) {
        String value = get(key, defaultValue);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return Integer.parseInt(defaultValue);
        }
    }

    private static boolean getBoolean(String key, String defaultValue) {
        return Boolean.parseBoolean(get(key, defaultValue));
    }

    private static String removeTrailingSlash(String value) {
        String result = value;

        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
}
