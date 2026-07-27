package services;

import http.JsonResponse;
import repositories.ReportRepository;
import repositories.ReportRepository.CreateReportRequest;
import repositories.ReportRepository.ReportResult;

import java.util.List;

public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService() {
        this.reportRepository = new ReportRepository();
    }

    public String createReport(long userId, String requestBody) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        Long reportCategoryId = readLongField(requestBody, "reportCategoryId");
        Long reservationId = readLongField(requestBody, "reservationId");
        Long ticketId = readLongField(requestBody, "ticketId");
        String title = readStringField(requestBody, "title");
        String description = readStringField(requestBody, "description");

        validateCreateReport(reportCategoryId, title, description);

        CreateReportRequest request = new CreateReportRequest(
                userId,
                reportCategoryId,
                reservationId,
                ticketId,
                title.trim(),
                description.trim()
        );

        ReportResult report = reportRepository.createReport(request);

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Report submitted successfully\","
                + "\"data\":" + buildReportJson(report)
                + "}";
    }

    public String getMyReports(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        List<ReportResult> reports = reportRepository.findReportsByUserId(userId);

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"data\":[");

        for (int i = 0; i < reports.size(); i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append(buildReportJson(reports.get(i)));
        }

        json.append("],\"count\":").append(reports.size()).append("}");
        return json.toString();
    }

    private void validateCreateReport(Long reportCategoryId, String title, String description) {
        if (reportCategoryId == null || reportCategoryId <= 0) {
            throw new IllegalArgumentException("reportCategoryId is required");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Report title is required");
        }

        if (title.trim().length() < 3) {
            throw new IllegalArgumentException("Report title must be at least 3 characters");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Report description is required");
        }

        if (description.trim().length() < 10) {
            throw new IllegalArgumentException("Report description must be at least 10 characters");
        }
    }

    private String buildReportJson(ReportResult report) {
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
                + "\"reservationId\":" + nullableLong(report.reservationId()) + ","
                + "\"ticketId\":" + nullableLong(report.ticketId()) + ","
                + "\"adminResponse\":\"" + JsonResponse.escape(report.adminResponse()) + "\""
                + "}";
    }

    private String nullableLong(Long value) {
        return value == null ? "null" : String.valueOf(value);
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

    private Long readLongField(String json, String fieldName) {
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

        int start = colonIndex + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }

        if (start == end) {
            return null;
        }

        try {
            long value = Long.parseLong(json.substring(start, end));
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
