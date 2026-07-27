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

    public static String getJwtSecret() {
        return getEnv("JWT_SECRET", "change-this-secret-in-local-env");
    }

    public static int getReservationTtlMinutes() {
        return getIntEnv("RESERVATION_TTL_MINUTES", 10);
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
