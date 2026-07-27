package config;

public final class AppConfig {
    private AppConfig() {
    }

    public static int getServerPort() {
        return getIntEnv("SERVER_PORT", 8080);
    }

    public static String getDbUrl() {
        return getEnv("DB_URL", "jdbc:postgresql://localhost:5432/sports_ticket_db");
    }

    public static String getDbUser() {
        return getEnv("DB_USER", "postgres");
    }

    public static String getDbPassword() {
        return getEnv("DB_PASSWORD", "postgres");
    }

    public static String getDbDriverClassName() {
        return getEnv("DB_DRIVER_CLASS_NAME", "org.postgresql.Driver");
    }

    public static int getDbLoginTimeoutSeconds() {
        return getIntEnv("DB_LOGIN_TIMEOUT_SECONDS", 10);
    }

    public static String getJwtSecret() {
        return getEnv("JWT_SECRET", "change-this-secret-in-local-env");
    }

    public static int getJwtExpirationMinutes() {
        return getIntEnv("JWT_EXPIRATION_MINUTES", 120);
    }

    public static int getReservationTtlMinutes() {
        return getIntEnv("RESERVATION_TTL_MINUTES", 10);
    }

    public static int getOtpTtlMinutes() {
        return getIntEnv("OTP_TTL_MINUTES", 5);
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static int getIntEnv(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
