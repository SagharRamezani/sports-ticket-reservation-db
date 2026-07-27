package services;

import http.JsonResponse;
import repositories.AdminRepository;
import repositories.AdminRepository.AdminReportResult;
import repositories.AdminRepository.ReservationAdminResult;
import repositories.AdminRepository.SuspiciousReservationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService() {
        this.adminRepository = new AdminRepository();
    }

    public String getReports(String roleCode) {
        requireAdminOrSupport(roleCode);

        List<AdminReportResult> reports = adminRepository.findAllReports();

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"data\":[");

        for (int i = 0; i < reports.size(); i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append(buildAdminReportJson(reports.get(i)));
        }

        json.append("],\"count\":").append(reports.size()).append("}");
        return json.toString();
    }

    public String updateReport(String roleCode, long reportId, String requestBody) {
        requireAdminOrSupport(roleCode);

        if (reportId <= 0) {
            throw new IllegalArgumentException("Invalid reportId");
        }

        String reportStatus = readStringField(requestBody, "reportStatus");
        String adminResponse = readStringField(requestBody, "adminResponse");

        validateReportUpdate(reportStatus, adminResponse);

        AdminReportResult updatedReport = adminRepository.updateReport(
                reportId,
                normalizeStatus(reportStatus),
                normalizeNullable(adminResponse)
        );

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Report updated successfully\","
                + "\"data\":" + buildAdminReportJson(updatedReport)
                + "}";
    }

    public String getSuspiciousReservations(String roleCode) {
        requireAdminOrSupport(roleCode);

        List<SuspiciousReservationResult> reservations = adminRepository.findSuspiciousReservations();

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"data\":[");

        for (int i = 0; i < reservations.size(); i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append(buildSuspiciousReservationJson(reservations.get(i)));
        }

        json.append("],\"count\":").append(reservations.size()).append("}");
        return json.toString();
    }

    public String updateReservationStatus(String roleCode, long reservationId, String requestBody) {
        requireAdminOrSupport(roleCode);

        if (reservationId <= 0) {
            throw new IllegalArgumentException("Invalid reservationId");
        }

        String reservationStatus = readStringField(requestBody, "reservationStatus");
        String adminNote = readStringField(requestBody, "adminNote");

        validateReservationStatus(reservationStatus);

        ReservationAdminResult updatedReservation = adminRepository.updateReservationStatus(
                reservationId,
                normalizeStatus(reservationStatus),
                normalizeNullable(adminNote)
        );

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Reservation updated successfully\","
                + "\"data\":" + buildReservationAdminJson(updatedReservation)
                + "}";
    }

    private void requireAdminOrSupport(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("Access denied");
        }

        String normalizedRole = roleCode.trim().toUpperCase(Locale.ROOT);

        if (!"ADMIN".equals(normalizedRole)
                && !"SUPPORT".equals(normalizedRole)
                && !"SUPER_ADMIN".equals(normalizedRole)) {
            throw new IllegalArgumentException("Access denied");
        }
    }

    private void validateReportUpdate(String reportStatus, String adminResponse) {
        if (reportStatus == null || reportStatus.isBlank()) {
            throw new IllegalArgumentException("reportStatus is required");
        }

        String normalizedStatus = normalizeStatus(reportStatus);

        if (!"OPEN".equals(normalizedStatus)
                && !"IN_PROGRESS".equals(normalizedStatus)
                && !"RESOLVED".equals(normalizedStatus)
                && !"REJECTED".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid reportStatus");
        }

        if (adminResponse != null && !adminResponse.isBlank() && adminResponse.trim().length() < 3) {
            throw new IllegalArgumentException("adminResponse must be at least 3 characters");
        }
    }

    private void validateReservationStatus(String reservationStatus) {
        if (reservationStatus == null || reservationStatus.isBlank()) {
            throw new IllegalArgumentException("reservationStatus is required");
        }

        String normalizedStatus = normalizeStatus(reservationStatus);

        if (!"PENDING".equals(normalizedStatus)
                && !"PAID".equals(normalizedStatus)
                && !"CANCELLED".equals(normalizedStatus)
                && !"EXPIRED".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid reservationStatus");
        }
    }

    private String buildAdminReportJson(AdminReportResult report) {
        return "{"
                + "\"reportId\":" + report.reportId() + ","
                + "\"reportStatus\":\"" + JsonResponse.escape(report.reportStatus()) + "\","
                + "\"title\":\"" + JsonResponse.escape(report.title()) + "\","
                + "\"description\":\"" + JsonResponse.escape(report.description()) + "\","
                + "\"createdAt\":\"" + JsonResponse.escape(report.createdAt()) + "\","
                + "\"updatedAt\":\"" + JsonResponse.escape(report.updatedAt()) + "\","
                + "\"category\":{"
                + "\"reportCategoryId\":" + nullableLong(report.reportCategoryId()) + ","
                + "\"categoryName\":\"" + JsonResponse.escape(report.categoryName()) + "\""
                + "},"
                + "\"user\":{"
                + "\"userId\":" + report.userId() + ","
                + "\"fullName\":\"" + JsonResponse.escape(report.fullName()) + "\","
                + "\"email\":\"" + JsonResponse.escape(report.email()) + "\","
                + "\"phoneNumber\":\"" + JsonResponse.escape(report.phoneNumber()) + "\""
                + "},"
                + "\"reservationId\":" + nullableLong(report.reservationId()) + ","
                + "\"ticketId\":" + nullableLong(report.ticketId()) + ","
                + "\"adminResponse\":\"" + JsonResponse.escape(report.adminResponse()) + "\""
                + "}";
    }

    private String buildSuspiciousReservationJson(SuspiciousReservationResult reservation) {
        return "{"
                + "\"reservationId\":" + reservation.reservationId() + ","
                + "\"userId\":" + reservation.userId() + ","
                + "\"userFullName\":\"" + JsonResponse.escape(reservation.userFullName()) + "\","
                + "\"reservationStatus\":\"" + JsonResponse.escape(reservation.reservationStatus()) + "\","
                + "\"reservedAt\":\"" + JsonResponse.escape(reservation.reservedAt()) + "\","
                + "\"expiresAt\":\"" + JsonResponse.escape(reservation.expiresAt()) + "\","
                + "\"ticketId\":" + reservation.ticketId() + ","
                + "\"ticketStatus\":\"" + JsonResponse.escape(reservation.ticketStatus()) + "\","
                + "\"price\":" + money(reservation.price()) + ","
                + "\"matchId\":" + reservation.matchId() + ","
                + "\"matchTitle\":\"" + JsonResponse.escape(reservation.matchTitle()) + "\","
                + "\"paymentStatus\":\"" + JsonResponse.escape(reservation.paymentStatus()) + "\","
                + "\"reason\":\"" + JsonResponse.escape(reservation.reason()) + "\""
                + "}";
    }

    private String buildReservationAdminJson(ReservationAdminResult reservation) {
        return "{"
                + "\"reservationId\":" + reservation.reservationId() + ","
                + "\"reservationStatus\":\"" + JsonResponse.escape(reservation.reservationStatus()) + "\","
                + "\"adminNote\":\"" + JsonResponse.escape(reservation.adminNote()) + "\","
                + "\"updatedAt\":\"" + JsonResponse.escape(reservation.updatedAt()) + "\""
                + "}";
    }

    private String normalizeStatus(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String nullableLong(Long value) {
        return value == null ? "null" : String.valueOf(value);
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }

        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String readStringField(String json, String fieldName) {
        if (json == null || json.isBlank()) {
            return null;
        }

        String pattern = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            return null;
        }

        int colonIndex = json.indexOf(":", keyIndex + pattern.length());
        if (colonIndex < 0) {
            return null;
        }

        int firstQuote = json.indexOf("\"", colonIndex + 1);
        if (firstQuote < 0) {
            return null;
        }

        int secondQuote = firstQuote + 1;
        boolean escaped = false;

        while (secondQuote < json.length()) {
            char current = json.charAt(secondQuote);

            if (current == '\\' && !escaped) {
                escaped = true;
            } else if (current == '"' && !escaped) {
                break;
            } else {
                escaped = false;
            }

            secondQuote++;
        }

        if (secondQuote >= json.length()) {
            return null;
        }

        return json.substring(firstQuote + 1, secondQuote)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
