package repositories;

import db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    public boolean existsByEmailOrPhone(String email, String phoneNumber) {
        String sql = """
                SELECT 1
                FROM users
                WHERE (? IS NOT NULL AND email = ?)
                   OR (? IS NOT NULL AND phone_number = ?)
                LIMIT 1
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, email);
            statement.setString(3, phoneNumber);
            statement.setString(4, phoneNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not check existing user", ex);
        }
    }

    public AuthUserResult createUser(CreateUserRequest request) {
        String sql = """
                INSERT INTO users
                    (
                        first_name,
                        last_name,
                        email,
                        phone_number,
                        password_hash,
                        role_id,
                        city_id,
                        is_active,
                        created_at
                    )
                VALUES
                    (
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        (SELECT role_id FROM roles WHERE role_code = 'CUSTOMER' LIMIT 1),
                        ?,
                        TRUE,
                        CURRENT_TIMESTAMP
                    )
                RETURNING
                    user_id,
                    first_name,
                    last_name,
                    email,
                    phone_number,
                    password_hash,
                    'CUSTOMER' AS role_code,
                    is_active
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.firstName());
            statement.setString(2, request.lastName());
            statement.setString(3, request.email());
            statement.setString(4, request.phoneNumber());
            statement.setString(5, request.passwordHash());

            if (request.cityId() == null) {
                statement.setObject(6, null);
            } else {
                statement.setLong(6, request.cityId());
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("User was not created");
                }

                return mapAuthUser(resultSet);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not create user", ex);
        }
    }

    public AuthUserResult findAuthUserByEmailOrPhone(String identifier) {
        String sql = """
                SELECT
                    u.user_id,
                    u.first_name,
                    u.last_name,
                    u.email,
                    u.phone_number,
                    u.password_hash,
                    r.role_code,
                    u.is_active
                FROM users u
                JOIN roles r ON u.role_id = r.role_id
                WHERE u.email = ?
                   OR u.phone_number = ?
                LIMIT 1
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, identifier);
            statement.setString(2, identifier);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Invalid email/phone or password");
                }

                return mapAuthUser(resultSet);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load user for login", ex);
        }
    }

    private AuthUserResult mapAuthUser(ResultSet resultSet) throws SQLException {
        return new AuthUserResult(
                resultSet.getLong("user_id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("email"),
                resultSet.getString("phone_number"),
                resultSet.getString("password_hash"),
                resultSet.getString("role_code"),
                resultSet.getBoolean("is_active")
        );
    }

    public record CreateUserRequest(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String passwordHash,
            Long cityId
    ) {
    }

    public record AuthUserResult(
            long userId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String passwordHash,
            String roleCode,
            boolean active
    ) {
    }
}
