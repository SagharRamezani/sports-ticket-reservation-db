package repositories;

import db.Database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<BookingHistoryResult> findBookingsByUserId(long userId) {
        String sql = """
                SELECT
                    r.reservation_id,
                    r.reservation_status,
                    r.reserved_at,
                    r.expires_at,
                    r.confirmed_at,
                    t.ticket_id,
                    t.ticket_status,
                    t.price,
                    COALESCE(tc.category_name, '') AS category_name,
                    m.match_id,
                    COALESCE(m.match_title, CONCAT('Match #', m.match_id)) AS match_title,
                    m.match_start_time,
                    COALESCE(v.venue_name, '') AS venue_name,
                    p.payment_id,
                    p.payment_status,
                    p.amount AS payment_amount
                FROM reservations r
                JOIN tickets t ON r.ticket_id = t.ticket_id
                JOIN matches m ON t.match_id = m.match_id
                LEFT JOIN ticket_categories tc ON t.ticket_category_id = tc.ticket_category_id
                LEFT JOIN venues v ON m.venue_id = v.venue_id
                LEFT JOIN payments p ON r.reservation_id = p.reservation_id
                WHERE r.user_id = ?
                ORDER BY r.reserved_at DESC, r.reservation_id DESC
                """;

        List<BookingHistoryResult> bookings = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(mapBookingHistory(resultSet));
                }
            }

            return bookings;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load user bookings", ex);
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

    private BookingHistoryResult mapBookingHistory(ResultSet resultSet) throws SQLException {
        long paymentIdValue = resultSet.getLong("payment_id");
        Long paymentId = resultSet.wasNull() ? null : paymentIdValue;

        return new BookingHistoryResult(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_status"),
                timestampToString(resultSet, "reserved_at"),
                timestampToString(resultSet, "expires_at"),
                timestampToString(resultSet, "confirmed_at"),
                resultSet.getLong("ticket_id"),
                resultSet.getString("ticket_status"),
                resultSet.getBigDecimal("price"),
                resultSet.getString("category_name"),
                resultSet.getLong("match_id"),
                resultSet.getString("match_title"),
                timestampToString(resultSet, "match_start_time"),
                resultSet.getString("venue_name"),
                paymentId,
                resultSet.getString("payment_status"),
                resultSet.getBigDecimal("payment_amount")
        );
    }

    private String timestampToString(ResultSet resultSet, String columnName) throws SQLException {
        if (resultSet.getTimestamp(columnName) == null) {
            return "";
        }

        return resultSet.getTimestamp(columnName).toLocalDateTime().toString();
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

    public record BookingHistoryResult(
            long reservationId,
            String reservationStatus,
            String reservedAt,
            String expiresAt,
            String confirmedAt,
            long ticketId,
            String ticketStatus,
            BigDecimal price,
            String categoryName,
            long matchId,
            String matchTitle,
            String matchStartTime,
            String venueName,
            Long paymentId,
            String paymentStatus,
            BigDecimal paymentAmount
    ) {
    }
}
