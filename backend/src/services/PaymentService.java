package services;

import http.JsonResponse;
import repositories.PaymentRepository;
import repositories.PaymentRepository.PaymentMethodSummary;
import repositories.PaymentRepository.PaymentResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
    }

    public String payReservation(long userId, long reservationId, String requestBody) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        if (reservationId <= 0) {
            throw new IllegalArgumentException("Invalid reservationId");
        }

        String methodCode = readStringField(requestBody, "methodCode");
        if (methodCode == null || methodCode.isBlank()) {
            methodCode = "BANK_CARD";
        }

        PaymentResult result = paymentRepository.payReservation(
                userId,
                reservationId,
                methodCode.trim().toUpperCase(Locale.ROOT)
        );

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Payment completed successfully\","
                + "\"data\":{"
                + "\"paymentId\":" + result.paymentId() + ","
                + "\"reservationId\":" + result.reservationId() + ","
                + "\"userId\":" + result.userId() + ","
                + "\"ticketId\":" + result.ticketId() + ","
                + "\"amount\":" + money(result.amount()) + ","
                + "\"paymentMethod\":\"" + JsonResponse.escape(result.paymentMethodCode()) + "\","
                + "\"paymentStatus\":\"" + JsonResponse.escape(result.paymentStatus()) + "\","
                + "\"reservationStatus\":\"" + JsonResponse.escape(result.reservationStatus()) + "\","
                + "\"ticketStatus\":\"" + JsonResponse.escape(result.ticketStatus()) + "\","
                + "\"transactionReference\":\"" + JsonResponse.escape(result.transactionReference()) + "\","
                + "\"paidAt\":\"" + JsonResponse.escape(result.paidAt()) + "\""
                + "}"
                + "}";
    }

    public String listActivePaymentMethods() {
        List<PaymentMethodSummary> methods = paymentRepository.findActivePaymentMethods();

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"data\":[");

        for (int i = 0; i < methods.size(); i++) {
            PaymentMethodSummary method = methods.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"paymentMethodId\":").append(method.paymentMethodId()).append(",")
                    .append("\"methodCode\":\"").append(JsonResponse.escape(method.methodCode())).append("\",")
                    .append("\"methodName\":\"").append(JsonResponse.escape(method.methodName())).append("\",")
                    .append("\"description\":\"").append(JsonResponse.escape(method.description())).append("\"")
                    .append("}");
        }

        json.append("]}");
        return json.toString();
    }

    public String buildRefundSummaryJson(boolean refundCreated, BigDecimal refundAmount, BigDecimal penaltyAmount) {
        return "{"
                + "\"refundCreated\":" + refundCreated + ","
                + "\"refundAmount\":" + money(refundAmount) + ","
                + "\"penaltyAmount\":" + money(penaltyAmount) + ","
                + "\"refundStatus\":\"" + (refundCreated ? "PROCESSED" : "NOT_REQUIRED") + "\""
                + "}";
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

        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote < 0) {
            return null;
        }

        return json.substring(firstQuote + 1, secondQuote);
    }
}
