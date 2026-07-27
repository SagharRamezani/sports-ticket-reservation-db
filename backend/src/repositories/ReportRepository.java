package repositories;

import db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {
    public ReportResult createReport(CreateReportRequest request) {
        String sql = """
                INSERT INTO reports
                    (
                        user_id,
                        report_category_id,
                        reservation_id,
                        ticket_id,
                        title,
                        description,
                        report_status,
                        created_at,
                        updated_at
                    )
                VALUES
                    (
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        'OPEN',
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                RETURNING report_id
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, request.userId());
            statement.setLong(2, request.reportCategoryId());

            if (request.reservationId() == null) {
                statement.setObject(3, null);
            } else {
                statement.setLong(3, request.reservationId());
            }

            if (request.ticketId() == null) {
                statement.setObject(4, null);
            } else {
                statement.setLong(4, request.ticketId());
            }

            statement.setString(5, request.title());
            statement.setString(6, request.description());

            long reportId;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Report was not created");
                }

                reportId = resultSet.getLong("report_id");
            }

            return findReportByIdForUser(reportId, request.userId());
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not create report", ex);
        }
    }

    public List<ReportResult> findReportsByUserId(long userId) {
        String sql = baseReportSelect()
                + """
                WHERE r.user_id = ?
                ORDER BY r.created_at DESC, r.report_id DESC
                """;

        List<ReportResult> reports = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(mapReport(resultSet));
                }
            }

            return reports;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load user reports", ex);
        }
    }

    private ReportResult findReportByIdForUser(long reportId, long userId) {
        String sql = baseReportSelect()
                + """
                WHERE r.report_id = ?
                  AND r.user_id = ?
                LIMIT 1
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, reportId);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Report not found");
                }

                return mapReport(resultSet);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load report", ex);
        }
    }

    private String baseReportSelect() {
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
                    r.reservation_id,
                    r.ticket_id,
                    COALESCE(r.admin_response, '') AS admin_response
                FROM reports r
                LEFT JOIN report_categories rc ON r.report_category_id = rc.report_category_id
                """;
    }

    private ReportResult mapReport(ResultSet resultSet) throws SQLException {
        long categoryIdValue = resultSet.getLong("report_category_id");
        Long categoryId = resultSet.wasNull() ? null : categoryIdValue;

        long reservationIdValue = resultSet.getLong("reservation_id");
        Long reservationId = resultSet.wasNull() ? null : reservationIdValue;

        long ticketIdValue = resultSet.getLong("ticket_id");
        Long ticketId = resultSet.wasNull() ? null : ticketIdValue;

        return new ReportResult(
                resultSet.getLong("report_id"),
                resultSet.getString("report_status"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                timestampToString(resultSet, "created_at"),
                timestampToString(resultSet, "updated_at"),
                categoryId,
                resultSet.getString("category_name"),
                reservationId,
                ticketId,
                resultSet.getString("admin_response")
        );
    }

    private String timestampToString(ResultSet resultSet, String columnName) throws SQLException {
        if (resultSet.getTimestamp(columnName) == null) {
            return "";
        }

        return resultSet.getTimestamp(columnName).toLocalDateTime().toString();
    }

    public record CreateReportRequest(
            long userId,
            long reportCategoryId,
            Long reservationId,
            Long ticketId,
            String title,
            String description
    ) {
    }

    public record ReportResult(
            long reportId,
            String reportStatus,
            String title,
            String description,
            String createdAt,
            String updatedAt,
            Long reportCategoryId,
            String categoryName,
            Long reservationId,
            Long ticketId,
            String adminResponse
    ) {
    }
}
