package repositories;

import db.Database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentRepository {
    public PaymentResult payReservation(long userId, long reservationId, String methodCode) {
        String findPaymentMethodSql = """
                SELECT payment_method_id, method_code
                FROM payment_methods
                WHERE method_code = ?
                  AND is_active = TRUE
                """;

        String lockReservationSql = """
                SELECT
                    r.reservation_id,
                    r.user_id,
                    r.ticket_id,
                    r.reservation_status,
                    r.expires_at,
                    t.price,
                    t.ticket_status
                FROM reservations r
                JOIN tickets t ON r.ticket_id = t.ticket_id
                WHERE r.reservation_id = ?
                FOR UPDATE
                """;

        String insertPaymentSql = """
                INSERT INTO payments
                    (
                        reservation_id,
                        user_id,
                        payment_method_id,
                        amount,
                        payment_status,
                        transaction_reference,
                        paid_at,
                        created_at
                    )
                VALUES
                    (?, ?, ?, ?, 'SUCCESS', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING payment_id, paid_at
                """;

        String updateReservationSql = """
                UPDATE reservations
                SET reservation_status = 'PAID',
                    confirmed_at = CURRENT_TIMESTAMP
                WHERE reservation_id = ?
                """;

        String updateTicketSql = """
                UPDATE tickets
                SET ticket_status = 'SOLD',
                    updated_at = CURRENT_TIMESTAMP
                WHERE ticket_id = ?
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                PaymentMethodData paymentMethod = findPaymentMethod(connection, findPaymentMethodSql, methodCode);
                ReservationPaymentData reservation = lockReservation(connection, lockReservationSql, reservationId);

                validateReservationForPayment(userId, reservation);

                String transactionReference = "LOCAL-" + UUID.randomUUID();

                long paymentId;
                String paidAt;

                try (PreparedStatement statement = connection.prepareStatement(insertPaymentSql)) {
                    statement.setLong(1, reservation.reservationId());
                    statement.setLong(2, userId);
                    statement.setLong(3, paymentMethod.paymentMethodId());
                    statement.setBigDecimal(4, reservation.price());
                    statement.setString(5, transactionReference);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Payment was not created");
                        }

                        paymentId = resultSet.getLong("payment_id");
                        paidAt = resultSet.getTimestamp("paid_at").toLocalDateTime().toString();
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(updateReservationSql)) {
                    statement.setLong(1, reservation.reservationId());
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(updateTicketSql)) {
                    statement.setLong(1, reservation.ticketId());
                    statement.executeUpdate();
                }

                connection.commit();

                return new PaymentResult(
                        paymentId,
                        reservation.reservationId(),
                        userId,
                        reservation.ticketId(),
                        reservation.price(),
                        paymentMethod.methodCode(),
                        "SUCCESS",
                        "PAID",
                        "SOLD",
                        transactionReference,
                        paidAt
                );
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not complete local payment", ex);
        }
    }

    public List<PaymentMethodSummary> findActivePaymentMethods() {
        String sql = """
                SELECT payment_method_id, method_code, method_name, description
                FROM payment_methods
                WHERE is_active = TRUE
                ORDER BY payment_method_id
                """;

        List<PaymentMethodSummary> methods = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                methods.add(new PaymentMethodSummary(
                        resultSet.getLong("payment_method_id"),
                        resultSet.getString("method_code"),
                        resultSet.getString("method_name"),
                        resultSet.getString("description")
                ));
            }

            return methods;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load payment methods", ex);
        }
    }

    private PaymentMethodData findPaymentMethod(Connection connection, String sql, String methodCode) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, methodCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Active payment method not found: " + methodCode);
                }

                return new PaymentMethodData(
                        resultSet.getLong("payment_method_id"),
                        resultSet.getString("method_code")
                );
            }
        }
    }

    private ReservationPaymentData lockReservation(Connection connection, String sql, long reservationId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Reservation not found");
                }

                Timestamp expiresAt = resultSet.getTimestamp("expires_at");

                return new ReservationPaymentData(
                        resultSet.getLong("reservation_id"),
                        resultSet.getLong("user_id"),
                        resultSet.getLong("ticket_id"),
                        resultSet.getString("reservation_status"),
                        expiresAt,
                        resultSet.getBigDecimal("price"),
                        resultSet.getString("ticket_status")
                );
            }
        }
    }

    private void validateReservationForPayment(long userId, ReservationPaymentData reservation) {
        if (reservation.userId() != userId) {
            throw new IllegalArgumentException("You are not allowed to pay this reservation");
        }

        if (!"PENDING".equalsIgnoreCase(reservation.reservationStatus())) {
            throw new IllegalArgumentException("Only pending reservations can be paid");
        }

        if (reservation.expiresAt() == null || reservation.expiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("Reservation is expired and cannot be paid");
        }

        if (!"RESERVED".equalsIgnoreCase(reservation.ticketStatus())) {
            throw new IllegalArgumentException("Ticket is not reserved and cannot be paid");
        }
    }

    private record PaymentMethodData(long paymentMethodId, String methodCode) {
    }

    private record ReservationPaymentData(
            long reservationId,
            long userId,
            long ticketId,
            String reservationStatus,
            Timestamp expiresAt,
            BigDecimal price,
            String ticketStatus
    ) {
    }

    public record PaymentResult(
            long paymentId,
            long reservationId,
            long userId,
            long ticketId,
            BigDecimal amount,
            String paymentMethodCode,
            String paymentStatus,
            String reservationStatus,
            String ticketStatus,
            String transactionReference,
            String paidAt
    ) {
    }

    public record PaymentMethodSummary(
            long paymentMethodId,
            String methodCode,
            String methodName,
            String description
    ) {
    }
}
