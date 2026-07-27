package services;

import http.JsonResponse;
import repositories.ReservationRepository;
import repositories.ReservationRepository.CancelReservationResult;
import repositories.ReservationRepository.CancellationPenalty;
import repositories.ReservationRepository.ExpiredReservationCleanupResult;
import repositories.ReservationRepository.ReservationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;

    public ReservationService() {
        this.reservationRepository = new ReservationRepository();
        this.paymentService = new PaymentService();
    }

    public String reserveTicket(long userId, long ticketId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        if (ticketId <= 0) {
            throw new IllegalArgumentException("Invalid ticketId");
        }

        ReservationResult result = reservationRepository.reserveTicket(userId, ticketId);

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Ticket reserved successfully\","
                + "\"data\":{"
                + "\"reservationId\":" + result.reservationId() + ","
                + "\"userId\":" + result.userId() + ","
                + "\"ticketId\":" + result.ticketId() + ","
                + "\"reservationStatus\":\"" + JsonResponse.escape(result.reservationStatus()) + "\","
                + "\"reservedAt\":\"" + JsonResponse.escape(result.reservedAt()) + "\","
                + "\"expiresAt\":\"" + JsonResponse.escape(result.expiresAt()) + "\""
                + "}"
                + "}";
    }

    public String getCancellationPenalty(long requesterUserId, String requesterRole, long reservationId) {
        CancellationPenalty penalty = reservationRepository.calculateCancellationPenalty(
                requesterUserId,
                requesterRole,
                reservationId
        );

        return "{"
                + "\"success\":true,"
                + "\"data\":{"
                + "\"reservationId\":" + penalty.reservationId() + ","
                + "\"ticketId\":" + penalty.ticketId() + ","
                + "\"ticketPrice\":" + money(penalty.ticketPrice()) + ","
                + "\"hoursBeforeMatch\":" + penalty.hoursBeforeMatch() + ","
                + "\"penaltyPercent\":" + money(penalty.penaltyPercent()) + ","
                + "\"penaltyAmount\":" + money(penalty.penaltyAmount()) + ","
                + "\"refundableAmount\":" + money(penalty.refundableAmount()) + ","
                + "\"matchStartTime\":\"" + JsonResponse.escape(penalty.matchStartTime()) + "\""
                + "}"
                + "}";
    }

    public String cancelReservation(long requesterUserId, String requesterRole, long reservationId, String requestBody) {
        String cancellationReason = readStringField(requestBody, "reason");
        if (cancellationReason == null || cancellationReason.isBlank()) {
            cancellationReason = "Cancelled through phase 3 reservation API";
        }

        CancelReservationResult result = reservationRepository.cancelReservation(
                requesterUserId,
                requesterRole,
                reservationId,
                cancellationReason
        );

        String refundSummary = paymentService.buildRefundSummaryJson(
                result.refundCreated(),
                result.refundAmount(),
                result.penaltyAmount()
        );

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Reservation cancelled successfully\","
                + "\"data\":{"
                + "\"reservationId\":" + result.reservationId() + ","
                + "\"ticketId\":" + result.ticketId() + ","
                + "\"reservationStatus\":\"" + JsonResponse.escape(result.reservationStatus()) + "\","
                + "\"ticketStatus\":\"" + JsonResponse.escape(result.ticketStatus()) + "\","
                + "\"cancellationReason\":\"" + JsonResponse.escape(result.cancellationReason()) + "\","
                + "\"refund\":" + refundSummary
                + "}"
                + "}";
    }

    public String cleanupExpiredReservations() {
        ExpiredReservationCleanupResult result = reservationRepository.cleanupExpiredReservations();

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Expired reservations cleaned up successfully\","
                + "\"data\":{"
                + "\"expiredReservationCount\":" + result.expiredReservationCount() + ","
                + "\"releasedTicketCount\":" + result.releasedTicketCount() + ","
                + "\"cancelledPendingPaymentCount\":" + result.cancelledPendingPaymentCount()
                + "}"
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
