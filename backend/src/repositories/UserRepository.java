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

    public boolean profileContactExistsForAnotherUser(long userId, String email, String phoneNumber) {
        String sql = """
                SELECT 1
                FROM users
                WHERE user_id <> ?
                  AND (
                    (? IS NOT NULL AND email = ?)
                    OR
                    (? IS NOT NULL AND phone_number = ?)
                  )
                LIMIT 1
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, email);
            statement.setString(3, email);
            statement.setString(4, phoneNumber);
            statement.setString(5, phoneNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not check duplicate profile contact", ex);
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

    public UserProfileResult findProfileById(long userId) {
        String sql = """
                SELECT
                    u.user_id,
                    u.first_name,
                    u.last_name,
                    u.email,
                    u.phone_number,
                    r.role_code,
                    u.city_id,
                    c.city_name,
                    u.is_active,
                    u.created_at
                FROM users u
                JOIN roles r ON u.role_id = r.role_id
                LEFT JOIN cities c ON u.city_id = c.city_id
                WHERE u.user_id = ?
                LIMIT 1
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("User profile not found");
                }

                return mapUserProfile(resultSet);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load user profile", ex);
        }
    }

    public UserProfileResult updateProfile(UpdateProfileRequest request) {
        String sql = """
                UPDATE users
                SET
                    first_name = COALESCE(?, first_name),
                    last_name = COALESCE(?, last_name),
                    email = COALESCE(?, email),
                    phone_number = COALESCE(?, phone_number),
                    city_id = COALESCE(?, city_id),
                    updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
                RETURNING user_id
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.firstName());
            statement.setString(2, request.lastName());
            statement.setString(3, request.email());
            statement.setString(4, request.phoneNumber());

            if (request.cityId() == null) {
                statement.setObject(5, null);
            } else {
                statement.setLong(5, request.cityId());
            }

            statement.setLong(6, request.userId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("User profile not found");
                }
            }

            return findProfileById(request.userId());
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not update user profile", ex);
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

    private UserProfileResult mapUserProfile(ResultSet resultSet) throws SQLException {
        long cityIdValue = resultSet.getLong("city_id");
        Long cityId = resultSet.wasNull() ? null : cityIdValue;

        return new UserProfileResult(
                resultSet.getLong("user_id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("email"),
                resultSet.getString("phone_number"),
                resultSet.getString("role_code"),
                cityId,
                resultSet.getString("city_name"),
                resultSet.getBoolean("is_active"),
                resultSet.getTimestamp("created_at") == null
                        ? ""
                        : resultSet.getTimestamp("created_at").toLocalDateTime().toString()
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

    public record UpdateProfileRequest(
            long userId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            Long cityId
    ) {
    }

    public record UserProfileResult(
            long userId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String roleCode,
            Long cityId,
            String cityName,
            boolean active,
            String createdAt
    ) {
    }
}
