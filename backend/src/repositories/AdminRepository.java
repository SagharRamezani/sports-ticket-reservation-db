package repositories;

import db.Database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminRepository {
    public List<AdminReportResult> findAllReports() {
        String sql = baseAdminReportSelect()
                + """
                ORDER BY r.created_at DESC, r.report_id DESC
                LIMIT 200
                """;

        List<AdminReportResult> reports = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                reports.add(mapAdminReport(resultSet));
            }

            return reports;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load admin reports", ex);
        }
    }

    public AdminReportResult updateReport(long reportId, String reportStatus, String adminResponse) {
        String sql = """
                UPDATE reports
                SET
                    report_status = ?,
                    admin_response = COALESCE(?, admin_response),
                    updated_at = CURRENT_TIMESTAMP
                WHERE report_id = ?
                RETURNING report_id
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reportStatus);
            statement.setString(2, adminResponse);
            statement.setLong(3, reportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Report not found");
                }
            }

            return findReportById(reportId);
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not update report", ex);
        }
    }

    public List<SuspiciousReservationResult> findSuspiciousReservations() {
        String sql = """
                SELECT
                    r.reservation_id,
                    r.user_id,
                    CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, '')) AS user_full_name,
                    r.reservation_status,
                    r.reserved_at,
                    r.expires_at,
                    t.ticket_id,
                    t.ticket_status,
                    t.price,
                    m.match_id,
                    COALESCE(m.match_title, CONCAT('Match #', m.match_id)) AS match_title,
                    COALESCE(p.payment_status, '') AS payment_status,
                    CASE
                        WHEN r.reservation_status = 'PENDING' AND r.expires_at < CURRENT_TIMESTAMP
                            THEN 'Pending reservation is expired but not marked as EXPIRED'
                        WHEN r.reservation_status = 'PAID' AND t.ticket_status <> 'SOLD'
                            THEN 'Paid reservation has ticket status mismatch'
                        WHEN r.reservation_status = 'CANCELLED' AND COALESCE(p.payment_status, '') = 'SUCCESS'
                            THEN 'Cancelled reservation has successful payment'
                        ELSE 'Needs support review'
                    END AS reason
                FROM reservations r
                JOIN users u ON r.user_id = u.user_id
                JOIN tickets t ON r.ticket_id = t.ticket_id
                JOIN ticket_categories tc ON t.ticket_category_id = tc.ticket_category_id
                JOIN matches m ON tc.match_id = m.match_id
                LEFT JOIN payments p ON r.reservation_id = p.reservation_id
                WHERE
                    (r.reservation_status = 'PENDING' AND r.expires_at < CURRENT_TIMESTAMP)
                    OR
                    (r.reservation_status = 'PAID' AND t.ticket_status <> 'SOLD')
                    OR
                    (r.reservation_status = 'CANCELLED' AND COALESCE(p.payment_status, '') = 'SUCCESS')
                ORDER BY r.reserved_at DESC, r.reservation_id DESC
                LIMIT 100
                """;

        List<SuspiciousReservationResult> reservations = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                reservations.add(mapSuspiciousReservation(resultSet));
            }

            return reservations;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load suspicious reservations", ex);
        }
    }

    public ReservationAdminResult updateReservationStatus(long reservationId, String reservationStatus, String adminNote) {
        String sql = """
                UPDATE reservations
                SET
                    reservation_status = ?,
                    admin_note = COALESCE(?, admin_note),
                    updated_at = CURRENT_TIMESTAMP
                WHERE reservation_id = ?
                RETURNING
                    reservation_id,
                    reservation_status,
                    COALESCE(admin_note, '') AS admin_note,
                    updated_at
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservationStatus);
            statement.setString(2, adminNote);
            statement.setLong(3, reservationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Reservation not found");
                }

                return new ReservationAdminResult(
                        resultSet.getLong("reservation_id"),
                        resultSet.getString("reservation_status"),
                        resultSet.getString("admin_note"),
                        timestampToString(resultSet, "updated_at")
                );
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not update reservation status", ex);
        }
    }

    private AdminReportResult findReportById(long reportId) {
        String sql = baseAdminReportSelect()
                + """
                WHERE r.report_id = ?
                LIMIT 1
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Report not found");
                }

                return mapAdminReport(resultSet);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load report", ex);
        }
    }

    private String baseAdminReportSelect() {
        return """
                SELECT
                    r.report_id,
                    r.report_status,
                    r.title,
                    r.description,
                    r.created_at,
                    r.updated_at,
                    r.report_category_id,
                    COALESCE(rc.category_name, '') AS category_name,
                    r.user_id,
                    CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, '')) AS full_name,
                    COALESCE(u.email, '') AS email,
                    COALESCE(u.phone_number, '') AS phone_number,
                    r.reservation_id,
                    r.ticket_id,
                    COALESCE(r.admin_response, '') AS admin_response
                FROM reports r
                LEFT JOIN report_categories rc ON r.report_category_id = rc.report_category_id
                JOIN users u ON r.user_id = u.user_id
                """;
    }

    private AdminReportResult mapAdminReport(ResultSet resultSet) throws SQLException {
        long categoryIdValue = resultSet.getLong("report_category_id");
        Long categoryId = resultSet.wasNull() ? null : categoryIdValue;

        long reservationIdValue = resultSet.getLong("reservation_id");
        Long reservationId = resultSet.wasNull() ? null : reservationIdValue;

        long ticketIdValue = resultSet.getLong("ticket_id");
        Long ticketId = resultSet.wasNull() ? null : ticketIdValue;

        return new AdminReportResult(
                resultSet.getLong("report_id"),
                resultSet.getString("report_status"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                timestampToString(resultSet, "created_at"),
                timestampToString(resultSet, "updated_at"),
                categoryId,
                resultSet.getString("category_name"),
                resultSet.getLong("user_id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("phone_number"),
                reservationId,
                ticketId,
                resultSet.getString("admin_response")
        );
    }

    private SuspiciousReservationResult mapSuspiciousReservation(ResultSet resultSet) throws SQLException {
        return new SuspiciousReservationResult(
                resultSet.getLong("reservation_id"),
                resultSet.getLong("user_id"),
                resultSet.getString("user_full_name"),
                resultSet.getString("reservation_status"),
                timestampToString(resultSet, "reserved_at"),
                timestampToString(resultSet, "expires_at"),
                resultSet.getLong("ticket_id"),
                resultSet.getString("ticket_status"),
                resultSet.getBigDecimal("price"),
                resultSet.getLong("match_id"),
                resultSet.getString("match_title"),
                resultSet.getString("payment_status"),
                resultSet.getString("reason")
        );
    }

    private String timestampToString(ResultSet resultSet, String columnName) throws SQLException {
        if (resultSet.getTimestamp(columnName) == null) {
            return "";
        }

        return resultSet.getTimestamp(columnName).toLocalDateTime().toString();
    }

    public record AdminReportResult(
            long reportId,
            String reportStatus,
            String title,
            String description,
            String createdAt,
            String updatedAt,
            Long reportCategoryId,
            String categoryName,
            long userId,
            String fullName,
            String email,
            String phoneNumber,
            Long reservationId,
            Long ticketId,
            String adminResponse
    ) {
    }

    public record SuspiciousReservationResult(
            long reservationId,
            long userId,
            String userFullName,
            String reservationStatus,
            String reservedAt,
            String expiresAt,
            long ticketId,
            String ticketStatus,
            BigDecimal price,
            long matchId,
            String matchTitle,
            String paymentStatus,
            String reason
    ) {
    }

    public record ReservationAdminResult(
            long reservationId,
            String reservationStatus,
            String adminNote,
            String updatedAt
    ) {
    }
}
