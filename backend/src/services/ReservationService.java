package services;

import http.JsonResponse;
import repositories.ReservationRepository;
import repositories.ReservationRepository.CancelReservationResult;
import repositories.ReservationRepository.CancellationPenalty;
import repositories.ReservationRepository.ReservationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService() {
        this.reservationRepository = new ReservationRepository();
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
                + "\"penaltyPercent\":" + money(penalty.penaltyPercent()) + ","
                + "\"penaltyAmount\":" + money(penalty.penaltyAmount()) + ","
                + "\"refundableAmount\":" + money(penalty.refundableAmount()) + ","
                + "\"matchStartTime\":\"" + JsonResponse.escape(penalty.matchStartTime()) + "\""
                + "}"
                + "}";
    }

    public String cancelReservation(long requesterUserId, String requesterRole, long reservationId) {
        CancelReservationResult result = reservationRepository.cancelReservation(
                requesterUserId,
                requesterRole,
                reservationId
        );

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Reservation cancelled successfully\","
                + "\"data\":{"
                + "\"reservationId\":" + result.reservationId() + ","
                + "\"ticketId\":" + result.ticketId() + ","
                + "\"reservationStatus\":\"" + JsonResponse.escape(result.reservationStatus()) + "\","
                + "\"ticketStatus\":\"" + JsonResponse.escape(result.ticketStatus()) + "\","
                + "\"refundCreated\":" + result.refundCreated() + ","
                + "\"refundAmount\":" + money(result.refundAmount()) + ","
                + "\"penaltyAmount\":" + money(result.penaltyAmount())
                + "}"
                + "}";
    }

    public String cleanupExpiredReservations() {
        int expiredCount = reservationRepository.cleanupExpiredReservations();

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Expired reservations cleaned up successfully\","
                + "\"data\":{"
                + "\"expiredReservationCount\":" + expiredCount
                + "}"
                + "}";
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }

        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
