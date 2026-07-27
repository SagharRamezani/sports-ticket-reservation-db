package services;

import http.JsonResponse;
import repositories.UserRepository;
import repositories.UserRepository.AuthUserResult;
import repositories.UserRepository.CreateUserRequest;
import security.JwtUtil;
import security.PasswordHasher;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public String signup(String requestBody) {
        String firstName = readStringField(requestBody, "firstName");
        String lastName = readStringField(requestBody, "lastName");
        String email = readStringField(requestBody, "email");
        String phoneNumber = readStringField(requestBody, "phoneNumber");
        String password = readStringField(requestBody, "password");
        Long cityId = readLongField(requestBody, "cityId");

        validateSignup(firstName, lastName, email, phoneNumber, password);

        if (userRepository.existsByEmailOrPhone(email, phoneNumber)) {
            throw new IllegalArgumentException("User with this email or phone number already exists");
        }

        String passwordHash = PasswordHasher.hashPassword(password);

        CreateUserRequest createUserRequest = new CreateUserRequest(
                firstName.trim(),
                lastName.trim(),
                normalizeNullable(email),
                normalizeNullable(phoneNumber),
                passwordHash,
                cityId
        );

        AuthUserResult user = userRepository.createUser(createUserRequest);
        String token = JwtUtil.generateToken(user.userId(), user.roleCode());

        return buildAuthResponse("Signup completed successfully", user, token);
    }

    public String login(String requestBody) {
        String identifier = readStringField(requestBody, "identifier");
        String password = readStringField(requestBody, "password");

        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Email or phone number is required");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        AuthUserResult user = userRepository.findAuthUserByEmailOrPhone(identifier.trim());

        if (!user.active()) {
            throw new IllegalArgumentException("User account is inactive");
        }

        if (!PasswordHasher.verifyPassword(password, user.passwordHash())) {
            throw new IllegalArgumentException("Invalid email/phone or password");
        }

        String token = JwtUtil.generateToken(user.userId(), user.roleCode());

        return buildAuthResponse("Login completed successfully", user, token);
    }

    private void validateSignup(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String password
    ) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phoneNumber != null && !phoneNumber.isBlank();

        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException("Email or phone number is required");
        }

        if (hasEmail && !email.contains("@")) {
            throw new IllegalArgumentException("Email format is invalid");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    private String buildAuthResponse(String message, AuthUserResult user, String token) {
        return "{"
                + "\"success\":true,"
                + "\"message\":\"" + JsonResponse.escape(message) + "\","
                + "\"data\":{"
                + "\"token\":\"" + JsonResponse.escape(token) + "\","
                + "\"user\":{"
                + "\"userId\":" + user.userId() + ","
                + "\"firstName\":\"" + JsonResponse.escape(user.firstName()) + "\","
                + "\"lastName\":\"" + JsonResponse.escape(user.lastName()) + "\","
                + "\"email\":\"" + JsonResponse.escape(user.email()) + "\","
                + "\"phoneNumber\":\"" + JsonResponse.escape(user.phoneNumber()) + "\","
                + "\"roleCode\":\"" + JsonResponse.escape(user.roleCode()) + "\","
                + "\"active\":" + user.active()
                + "}"
                + "}"
                + "}";
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
