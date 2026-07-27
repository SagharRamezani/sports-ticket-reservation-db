package services;

import http.JsonResponse;
import repositories.UserRepository;
import repositories.UserRepository.BookingHistoryResult;
import repositories.UserRepository.UpdateProfileRequest;
import repositories.UserRepository.UserProfileResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public String getMyProfile(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        UserProfileResult profile = userRepository.findProfileById(userId);

        return "{"
                + "\"success\":true,"
                + "\"data\":"
                + buildProfileJson(profile)
                + "}";
    }

    public String updateMyProfile(long userId, String requestBody) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        String firstName = readStringField(requestBody, "firstName");
        String lastName = readStringField(requestBody, "lastName");
        String email = readStringField(requestBody, "email");
        String phoneNumber = readStringField(requestBody, "phoneNumber");
        Long cityId = readLongField(requestBody, "cityId");

        validateUpdate(firstName, lastName, email, phoneNumber);

        if (userRepository.profileContactExistsForAnotherUser(userId, email, phoneNumber)) {
            throw new IllegalArgumentException("Email or phone number is already used by another user");
        }

        UpdateProfileRequest updateRequest = new UpdateProfileRequest(
                userId,
                normalizeNullable(firstName),
                normalizeNullable(lastName),
                normalizeNullable(email),
                normalizeNullable(phoneNumber),
                cityId
        );

        UserProfileResult updatedProfile = userRepository.updateProfile(updateRequest);

        return "{"
                + "\"success\":true,"
                + "\"message\":\"Profile updated successfully\","
                + "\"data\":"
                + buildProfileJson(updatedProfile)
                + "}";
    }

    public String getMyBookings(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        List<BookingHistoryResult> bookings = userRepository.findBookingsByUserId(userId);

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"data\":[");

        for (int i = 0; i < bookings.size(); i++) {
            BookingHistoryResult booking = bookings.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append(buildBookingJson(booking));
        }

        json.append("],\"count\":").append(bookings.size()).append("}");
        return json.toString();
    }

    private void validateUpdate(String firstName, String lastName, String email, String phoneNumber) {
        if (firstName != null && !firstName.isBlank() && firstName.trim().length() < 2) {
            throw new IllegalArgumentException("First name must be at least 2 characters");
        }

        if (lastName != null && !lastName.isBlank() && lastName.trim().length() < 2) {
            throw new IllegalArgumentException("Last name must be at least 2 characters");
        }

        if (email != null && !email.isBlank() && !email.contains("@")) {
            throw new IllegalArgumentException("Email format is invalid");
        }

        if (phoneNumber != null && !phoneNumber.isBlank() && phoneNumber.trim().length() < 8) {
            throw new IllegalArgumentException("Phone number format is invalid");
        }
    }

    private String buildProfileJson(UserProfileResult profile) {
        return "{"
                + "\"userId\":" + profile.userId() + ","
                + "\"firstName\":\"" + JsonResponse.escape(profile.firstName()) + "\","
                + "\"lastName\":\"" + JsonResponse.escape(profile.lastName()) + "\","
                + "\"email\":\"" + JsonResponse.escape(profile.email()) + "\","
                + "\"phoneNumber\":\"" + JsonResponse.escape(profile.phoneNumber()) + "\","
                + "\"roleCode\":\"" + JsonResponse.escape(profile.roleCode()) + "\","
                + "\"cityId\":" + nullableLong(profile.cityId()) + ","
                + "\"cityName\":\"" + JsonResponse.escape(profile.cityName()) + "\","
                + "\"active\":" + profile.active() + ","
                + "\"createdAt\":\"" + JsonResponse.escape(profile.createdAt()) + "\""
                + "}";
    }

    private String buildBookingJson(BookingHistoryResult booking) {
        return "{"
                + "\"reservationId\":" + booking.reservationId() + ","
                + "\"reservationStatus\":\"" + JsonResponse.escape(booking.reservationStatus()) + "\","
                + "\"reservedAt\":\"" + JsonResponse.escape(booking.reservedAt()) + "\","
                + "\"expiresAt\":\"" + JsonResponse.escape(booking.expiresAt()) + "\","
                + "\"confirmedAt\":\"" + JsonResponse.escape(booking.confirmedAt()) + "\","
                + "\"ticket\":{"
                + "\"ticketId\":" + booking.ticketId() + ","
                + "\"ticketStatus\":\"" + JsonResponse.escape(booking.ticketStatus()) + "\","
                + "\"categoryName\":\"" + JsonResponse.escape(booking.categoryName()) + "\","
                + "\"price\":" + money(booking.price())
                + "},"
                + "\"match\":{"
                + "\"matchId\":" + booking.matchId() + ","
                + "\"matchTitle\":\"" + JsonResponse.escape(booking.matchTitle()) + "\","
                + "\"matchStartTime\":\"" + JsonResponse.escape(booking.matchStartTime()) + "\","
                + "\"venueName\":\"" + JsonResponse.escape(booking.venueName()) + "\""
                + "},"
                + "\"payment\":{"
                + "\"paymentId\":" + nullableLong(booking.paymentId()) + ","
                + "\"paymentStatus\":\"" + JsonResponse.escape(booking.paymentStatus()) + "\","
                + "\"amount\":" + money(booking.paymentAmount())
                + "}"
                + "}";
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }

        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String nullableLong(Long value) {
        return value == null ? "null" : String.valueOf(value);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
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
