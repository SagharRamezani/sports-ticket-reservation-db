package db;

import config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static boolean driverLoaded = false;

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        loadDriverIfNeeded();
        DriverManager.setLoginTimeout(AppConfig.getDbLoginTimeoutSeconds());

        return DriverManager.getConnection(
                AppConfig.getDbUrl(),
                AppConfig.getDbUser(),
                AppConfig.getDbPassword()
        );
    }

    public static boolean testConnection() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            return true;
        } catch (SQLException ex) {
            System.err.println("Database connection test failed: " + ex.getMessage());
            return false;
        }
    }

    public static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }

        try {
            resource.close();
        } catch (Exception ignored) {
            // Ignore close errors because the original database operation result is more important.
        }
    }

    private static synchronized void loadDriverIfNeeded() throws SQLException {
        if (driverLoaded) {
            return;
        }

        try {
            Class.forName(AppConfig.getDbDriverClassName());
            driverLoaded = true;
        } catch (ClassNotFoundException ex) {
            throw new SQLException(
                    "PostgreSQL JDBC driver was not found. Put the JDBC jar in backend/lib and include it in classpath.",
                    ex
            );
        }
    }
}
