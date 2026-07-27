package repositories;

import config.AppConfig;
import db.Database;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationRepository {
    public ReservationResult reserveTicket(long userId, long ticketId) {
        String lockTicketSql = """
                SELECT ticket_id, ticket_status
                FROM tickets
                WHERE ticket_id = ?
                FOR UPDATE
                """;

        String insertReservationSql = """
                INSERT INTO reservations
                    (user_id, ticket_id, reservation_status, reserved_at, expires_at)
                VALUES
                    (?, ?, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + (? * INTERVAL '1 minute'))
                RETURNING reservation_id, user_id, ticket_id, reservation_status, reserved_at, expires_at
                """;

        String updateTicketSql = """
                UPDATE tickets
                SET ticket_status = 'RESERVED',
                    updated_at = CURRENT_TIMESTAMP
                WHERE ticket_id = ?
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String currentStatus;

                try (PreparedStatement statement = connection.prepareStatement(lockTicketSql)) {
                    statement.setLong(1, ticketId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalArgumentException("Ticket not found");
                        }

                        currentStatus = resultSet.getString("ticket_status");
                    }
                }

                if (!"AVAILABLE".equalsIgnoreCase(currentStatus)) {
                    throw new IllegalArgumentException("Ticket is not available for reservation");
                }

                ReservationResult reservation;

                try (PreparedStatement statement = connection.prepareStatement(insertReservationSql)) {
                    statement.setLong(1, userId);
                    statement.setLong(2, ticketId);
                    statement.setInt(3, AppConfig.getReservationTtlMinutes());

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Reservation was not created");
                        }

                        reservation = mapReservationResult(resultSet);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(updateTicketSql)) {
                    statement.setLong(1, ticketId);
                    statement.executeUpdate();
                }

                connection.commit();
                return reservation;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not reserve ticket", ex);
        }
    }

    public CancellationPenalty calculateCancellationPenalty(long requesterUserId, String requesterRole, long reservationId) {
        ReservationPenaltyData data = findReservationPenaltyData(requesterUserId, requesterRole, reservationId);
        long hoursBeforeMatch = Math.max(0, data.hoursBeforeMatch());
        BigDecimal penaltyPercent = findPenaltyPercent(data.matchId(), data.ticketCategoryId(), hoursBeforeMatch);

        BigDecimal penaltyAmount = data.price()
                .multiply(penaltyPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal refundableAmount = data.price().subtract(penaltyAmount);
        if (refundableAmount.compareTo(BigDecimal.ZERO) < 0) {
            refundableAmount = BigDecimal.ZERO;
        }

        return new CancellationPenalty(
                data.reservationId(),
                data.ticketId(),
                data.price(),
                hoursBeforeMatch,
                penaltyPercent,
                penaltyAmount,
                refundableAmount,
                data.matchStartTime().toString()
        );
    }

    public CancelReservationResult cancelReservation(
            long requesterUserId,
            String requesterRole,
            long reservationId,
            String cancellationReason
    ) {
        String lockReservationSql = """
                SELECT
                    r.reservation_id,
                    r.user_id,
                    r.ticket_id,
                    r.reservation_status,
                    t.ticket_status,
                    t.price,
                    t.ticket_category_id,
                    m.match_id,
                    m.match_start_time
                FROM reservations r
                JOIN tickets t ON r.ticket_id = t.ticket_id
                JOIN matches m ON t.match_id = m.match_id
                WHERE r.reservation_id = ?
                FOR UPDATE
                """;

        String successfulPaymentSql = """
                SELECT payment_id, amount
                FROM payments
                WHERE reservation_id = ?
                  AND payment_status = 'SUCCESS'
                ORDER BY created_at DESC
                LIMIT 1
                """;

        String updateReservationSql = """
                UPDATE reservations
                SET reservation_status = 'CANCELLED',
                    cancelled_at = CURRENT_TIMESTAMP,
                    cancellation_reason = ?
                WHERE reservation_id = ?
                """;

        String updateTicketSql = """
                UPDATE tickets
                SET ticket_status = 'CANCELLED',
                    updated_at = CURRENT_TIMESTAMP
                WHERE ticket_id = ?
                """;

        String updatePaymentSql = """
                UPDATE payments
                SET payment_status = 'REFUNDED'
                WHERE payment_id = ?
                """;

        String insertRefundSql = """
                INSERT INTO refunds
                    (reservation_id, payment_id, amount, penalty_amount, refund_status, requested_at, processed_at, description)
                VALUES
                    (?, ?, ?, ?, 'PROCESSED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                ReservationPenaltyData data;

                try (PreparedStatement statement = connection.prepareStatement(lockReservationSql)) {
                    statement.setLong(1, reservationId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalArgumentException("Reservation not found");
                        }

                        long reservationOwnerId = resultSet.getLong("user_id");
                        ensureRequesterCanAccess(requesterUserId, requesterRole, reservationOwnerId);

                        String reservationStatus = resultSet.getString("reservation_status");
                        if ("CANCELLED".equalsIgnoreCase(reservationStatus)) {
                            throw new IllegalArgumentException("Reservation is already cancelled");
                        }
                        if ("EXPIRED".equalsIgnoreCase(reservationStatus)) {
                            throw new IllegalArgumentException("Expired reservation cannot be cancelled");
                        }

                        data = new ReservationPenaltyData(
                                resultSet.getLong("reservation_id"),
                                reservationOwnerId,
                                resultSet.getLong("ticket_id"),
                                resultSet.getLong("match_id"),
                                resultSet.getLong("ticket_category_id"),
                                resultSet.getBigDecimal("price"),
                                resultSet.getTimestamp("match_start_time").toLocalDateTime()
                        );
                    }
                }

                long hoursBeforeMatch = Math.max(0, data.hoursBeforeMatch());
                BigDecimal penaltyPercent = findPenaltyPercent(connection, data.matchId(), data.ticketCategoryId(), hoursBeforeMatch);
                BigDecimal penaltyAmount = data.price()
                        .multiply(penaltyPercent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                BigDecimal refundAmount = BigDecimal.ZERO;
                Long paymentId = null;

                try (PreparedStatement statement = connection.prepareStatement(successfulPaymentSql)) {
                    statement.setLong(1, reservationId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            paymentId = resultSet.getLong("payment_id");
                            BigDecimal paymentAmount = resultSet.getBigDecimal("amount");
                            refundAmount = paymentAmount.subtract(penaltyAmount);

                            if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                                refundAmount = BigDecimal.ZERO;
                            }
                        }
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(updateReservationSql)) {
                    statement.setString(1, cancellationReason);
                    statement.setLong(2, reservationId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(updateTicketSql)) {
                    statement.setLong(1, data.ticketId());
                    statement.executeUpdate();
                }

                boolean refundCreated = paymentId != null;

                if (refundCreated) {
                    try (PreparedStatement statement = connection.prepareStatement(updatePaymentSql)) {
                        statement.setLong(1, paymentId);
                        statement.executeUpdate();
                    }

                    try (PreparedStatement statement = connection.prepareStatement(insertRefundSql)) {
                        statement.setLong(1, reservationId);
                        statement.setLong(2, paymentId);
                        statement.setBigDecimal(3, refundAmount);
                        statement.setBigDecimal(4, penaltyAmount);
                        statement.setString(5, "Refund created by cancellation API. Reason: " + cancellationReason);
                        statement.executeUpdate();
                    }
                }

                connection.commit();

                return new CancelReservationResult(
                        reservationId,
                        data.ticketId(),
                        "CANCELLED",
                        "CANCELLED",
                        cancellationReason,
                        refundCreated,
                        refundAmount,
                        penaltyAmount
                );
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not cancel reservation", ex);
        }
    }

    public ExpiredReservationCleanupResult cleanupExpiredReservations() {
        String selectExpiredTicketsSql = """
                SELECT ticket_id
                FROM reservations
                WHERE reservation_status = 'PENDING'
                  AND expires_at < CURRENT_TIMESTAMP
                """;

        String expireReservationsSql = """
                UPDATE reservations
                SET reservation_status = 'EXPIRED',
                    cancelled_at = CURRENT_TIMESTAMP,
                    cancellation_reason = 'Reservation expired before payment'
                WHERE reservation_status = 'PENDING'
                  AND expires_at < CURRENT_TIMESTAMP
                """;

        String releaseTicketsSql = """
                UPDATE tickets
                SET ticket_status = 'AVAILABLE',
                    updated_at = CURRENT_TIMESTAMP
                WHERE ticket_id = ANY (?)
                  AND ticket_status = 'RESERVED'
                """;

        String cancelPendingPaymentsSql = """
                UPDATE payments
                SET payment_status = 'CANCELLED'
                WHERE reservation_id = ANY (?)
                  AND payment_status = 'PENDING'
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                List<Long> ticketIds = new ArrayList<>();
                List<Long> reservationIds = new ArrayList<>();

                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT reservation_id, ticket_id
                        FROM reservations
                        WHERE reservation_status = 'PENDING'
                          AND expires_at < CURRENT_TIMESTAMP
                        """);
                     ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        reservationIds.add(resultSet.getLong("reservation_id"));
                        ticketIds.add(resultSet.getLong("ticket_id"));
                    }
                }

                int expiredCount;
                try (PreparedStatement statement = connection.prepareStatement(expireReservationsSql)) {
                    expiredCount = statement.executeUpdate();
                }

                int releasedTicketCount = 0;
                if (!ticketIds.isEmpty()) {
                    Array ticketIdArray = connection.createArrayOf("BIGINT", ticketIds.toArray());
                    try (PreparedStatement statement = connection.prepareStatement(releaseTicketsSql)) {
                        statement.setArray(1, ticketIdArray);
                        releasedTicketCount = statement.executeUpdate();
                    }
                }

                int cancelledPendingPaymentCount = 0;
                if (!reservationIds.isEmpty()) {
                    Array reservationIdArray = connection.createArrayOf("BIGINT", reservationIds.toArray());
                    try (PreparedStatement statement = connection.prepareStatement(cancelPendingPaymentsSql)) {
                        statement.setArray(1, reservationIdArray);
                        cancelledPendingPaymentCount = statement.executeUpdate();
                    }
                }

                connection.commit();
                return new ExpiredReservationCleanupResult(
                        expiredCount,
                        releasedTicketCount,
                        cancelledPendingPaymentCount
                );
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not cleanup expired reservations", ex);
        }
    }

    private ReservationPenaltyData findReservationPenaltyData(long requesterUserId, String requesterRole, long reservationId) {
        String sql = """
                SELECT
                    r.reservation_id,
                    r.user_id,
                    r.ticket_id,
                    t.price,
                    t.ticket_category_id,
                    m.match_id,
                    m.match_start_time
                FROM reservations r
                JOIN tickets t ON r.ticket_id = t.ticket_id
                JOIN matches m ON t.match_id = m.match_id
                WHERE r.reservation_id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Reservation not found");
                }

                long reservationOwnerId = resultSet.getLong("user_id");
                ensureRequesterCanAccess(requesterUserId, requesterRole, reservationOwnerId);

                return new ReservationPenaltyData(
                        resultSet.getLong("reservation_id"),
                        reservationOwnerId,
                        resultSet.getLong("ticket_id"),
                        resultSet.getLong("match_id"),
                        resultSet.getLong("ticket_category_id"),
                        resultSet.getBigDecimal("price"),
                        resultSet.getTimestamp("match_start_time").toLocalDateTime()
                );
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not calculate cancellation penalty", ex);
        }
    }

    private BigDecimal findPenaltyPercent(long matchId, long ticketCategoryId, long hoursBeforeMatch) {
        try (Connection connection = Database.getConnection()) {
            return findPenaltyPercent(connection, matchId, ticketCategoryId, hoursBeforeMatch);
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not find cancellation policy", ex);
        }
    }

    private BigDecimal findPenaltyPercent(
            Connection connection,
            long matchId,
            long ticketCategoryId,
            long hoursBeforeMatch
    ) throws SQLException {
        String sql = """
                SELECT penalty_percent
                FROM cancellation_policies
                WHERE (match_id = ? OR match_id IS NULL)
                  AND (ticket_category_id = ? OR ticket_category_id IS NULL)
                  AND hours_before_match <= ?
                ORDER BY
                    CASE WHEN match_id = ? THEN 0 ELSE 1 END,
                    CASE WHEN ticket_category_id = ? THEN 0 ELSE 1 END,
                    hours_before_match DESC
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, matchId);
            statement.setLong(2, ticketCategoryId);
            statement.setLong(3, hoursBeforeMatch);
            statement.setLong(4, matchId);
            statement.setLong(5, ticketCategoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("penalty_percent");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private void ensureRequesterCanAccess(long requesterUserId, String requesterRole, long ownerUserId) {
        boolean isSupport = requesterRole != null && requesterRole.equalsIgnoreCase("SUPPORT");

        if (!isSupport && requesterUserId != ownerUserId) {
            throw new IllegalArgumentException("You are not allowed to access this reservation");
        }
    }

    private ReservationResult mapReservationResult(ResultSet resultSet) throws SQLException {
        return new ReservationResult(
                resultSet.getLong("reservation_id"),
                resultSet.getLong("user_id"),
                resultSet.getLong("ticket_id"),
                resultSet.getString("reservation_status"),
                resultSet.getTimestamp("reserved_at").toLocalDateTime().toString(),
                resultSet.getTimestamp("expires_at").toLocalDateTime().toString()
        );
    }

    public record ReservationResult(
            long reservationId,
            long userId,
            long ticketId,
            String reservationStatus,
            String reservedAt,
            String expiresAt
    ) {
    }

    public record CancellationPenalty(
            long reservationId,
            long ticketId,
            BigDecimal ticketPrice,
            long hoursBeforeMatch,
            BigDecimal penaltyPercent,
            BigDecimal penaltyAmount,
            BigDecimal refundableAmount,
            String matchStartTime
    ) {
    }

    public record CancelReservationResult(
            long reservationId,
            long ticketId,
            String reservationStatus,
            String ticketStatus,
            String cancellationReason,
            boolean refundCreated,
            BigDecimal refundAmount,
            BigDecimal penaltyAmount
    ) {
    }

    public record ExpiredReservationCleanupResult(
            int expiredReservationCount,
            int releasedTicketCount,
            int cancelledPendingPaymentCount
    ) {
    }

    private record ReservationPenaltyData(
            long reservationId,
            long userId,
            long ticketId,
            long matchId,
            long ticketCategoryId,
            BigDecimal price,
            LocalDateTime matchStartTime
    ) {
        long hoursBeforeMatch() {
            return Duration.between(LocalDateTime.now(), matchStartTime).toHours();
        }
    }
}
