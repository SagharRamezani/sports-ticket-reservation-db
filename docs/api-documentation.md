# Phase 3 API Documentation

Project: Sports Match Ticket Reservation System  
Database: PostgreSQL  
Backend: Raw Java with built-in HttpServer  
DB Access: JDBC with direct SQL and PreparedStatement  
Response Format: JSON

---

## 1. General Notes

### Base URL

```text
http://localhost:8080
```

### Headers

For protected APIs:

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

For public APIs:

```http
Content-Type: application/json
```

### Standard Success Response

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {}
}
```

### Standard Error Response

```json
{
  "success": false,
  "message": "Error message"
}
```

---

## 2. Auth APIs

Owner: Shamim

### 2.1 Signup

```http
POST /api/auth/signup
```

Creates a new customer account. Password is hashed before saving.

#### Request Body

```json
{
  "firstName": "Shamim",
  "lastName": "Ahmadi",
  "email": "shamim@example.com",
  "phoneNumber": "09120000000",
  "password": "123456",
  "cityId": 1
}
```

#### Success Response

```json
{
  "success": true,
  "message": "Signup completed successfully",
  "data": {
    "token": "<JWT_TOKEN>",
    "user": {
      "userId": 1,
      "firstName": "Shamim",
      "lastName": "Ahmadi",
      "email": "shamim@example.com",
      "phoneNumber": "09120000000",
      "roleCode": "CUSTOMER",
      "active": true
    }
  }
}
```

#### Validation Rules

- `firstName` is required.
- `lastName` is required.
- At least one of `email` or `phoneNumber` is required.
- `password` must be at least 6 characters.
- Duplicate email or phone number is not allowed.

---

### 2.2 Login

```http
POST /api/auth/login
```

Logs in a user by email or phone number and returns JWT.

#### Request Body

```json
{
  "identifier": "shamim@example.com",
  "password": "123456"
}
```

#### Success Response

```json
{
  "success": true,
  "message": "Login completed successfully",
  "data": {
    "token": "<JWT_TOKEN>",
    "user": {
      "userId": 1,
      "firstName": "Shamim",
      "lastName": "Ahmadi",
      "email": "shamim@example.com",
      "phoneNumber": "09120000000",
      "roleCode": "CUSTOMER",
      "active": true
    }
  }
}
```

#### Validation Rules

- `identifier` is required.
- `password` is required.
- Password is verified with `PasswordHasher.verifyPassword`.
- Inactive users cannot log in.

---

### 2.3 Request OTP

```http
POST /api/auth/request-otp
```

Generates a mock OTP for login verification. Real SMS or email sending is not required in this phase.

#### Request Body

```json
{
  "identifier": "shamim@example.com"
}
```

#### Success Response

```json
{
  "success": true,
  "message": "OTP generated successfully. Sending SMS/email is mocked in this phase.",
  "data": {
    "identifier": "shamim@example.com",
    "mockOtp": "123456",
    "ttlMinutes": 5,
    "expiresAt": 1760000000
  }
}
```

#### Notes

- OTP is six digits.
- OTP is stored temporarily in memory.
- OTP TTL is controlled by `AppConfig.getOtpTtlMinutes()`.
- Redis integration can be added later through CacheService.

---

### 2.4 Verify OTP

```http
POST /api/auth/verify-otp
```

Verifies OTP and returns JWT.

#### Request Body

```json
{
  "identifier": "shamim@example.com",
  "otpCode": "123456"
}
```

#### Success Response

```json
{
  "success": true,
  "message": "OTP verified successfully",
  "data": {
    "token": "<JWT_TOKEN>",
    "user": {
      "userId": 1,
      "firstName": "Shamim",
      "lastName": "Ahmadi",
      "email": "shamim@example.com",
      "phoneNumber": "09120000000",
      "roleCode": "CUSTOMER",
      "active": true
    }
  }
}
```

#### Validation Rules

- `identifier` is required.
- `otpCode` is required.
- Expired OTP is rejected.
- Invalid OTP is rejected.
- OTP is deleted after successful verification.

---

## 3. User APIs

Owner: Shamim

All user APIs require JWT.

---

### 3.1 Get My Profile

```http
GET /api/users/me
```

Returns the profile of the logged-in user.

#### Headers

```http
Authorization: Bearer <JWT_TOKEN>
```

#### Success Response

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "firstName": "Shamim",
    "lastName": "Ahmadi",
    "email": "shamim@example.com",
    "phoneNumber": "09120000000",
    "roleCode": "CUSTOMER",
    "cityId": 1,
    "cityName": "Tehran",
    "active": true,
    "createdAt": "2026-01-01T10:00:00"
  }
}
```

---

### 3.2 Update My Profile

```http
PATCH /api/users/me
```

Updates the profile of the logged-in user.

#### Headers

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

#### Request Body

```json
{
  "firstName": "Shamim",
  "lastName": "Mohammadi",
  "email": "new-shamim@example.com",
  "phoneNumber": "09121111111",
  "cityId": 2
}
```

#### Success Response

```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "userId": 1,
    "firstName": "Shamim",
    "lastName": "Mohammadi",
    "email": "new-shamim@example.com",
    "phoneNumber": "09121111111",
    "roleCode": "CUSTOMER",
    "cityId": 2,
    "cityName": "Shiraz",
    "active": true,
    "createdAt": "2026-01-01T10:00:00"
  }
}
```

#### Validation Rules

- Email format must contain `@`.
- Phone number must have valid minimum length.
- Email or phone number cannot be used by another user.

---

### 3.3 Get My Bookings

```http
GET /api/users/me/bookings
```

Returns reservation and payment history of the logged-in user.

#### Headers

```http
Authorization: Bearer <JWT_TOKEN>
```

#### Success Response

```json
{
  "success": true,
  "data": [
    {
      "reservationId": 10,
      "reservationStatus": "PAID",
      "reservedAt": "2026-01-01T10:00:00",
      "expiresAt": "2026-01-01T10:10:00",
      "confirmedAt": "2026-01-01T10:05:00",
      "ticket": {
        "ticketId": 100,
        "ticketStatus": "SOLD",
        "categoryName": "VIP",
        "price": 500000.00
      },
      "match": {
        "matchId": 20,
        "matchTitle": "Team A vs Team B",
        "matchStartTime": "2026-01-05T18:00:00",
        "venueName": "Azadi Stadium"
      },
      "payment": {
        "paymentId": 30,
        "paymentStatus": "SUCCESS",
        "amount": 500000.00
      }
    }
  ],
  "count": 1
}
```

#### Security Rule

Only bookings belonging to the logged-in user are returned.

---

## 4. Ticket and Search APIs

Owner: Sarina

### 4.1 Cities

```http
GET /api/cities
```

Returns city list.

### 4.2 Venues

```http
GET /api/venues
```

Returns venue list.

### 4.3 Ticket List

```http
GET /api/tickets
```

Returns available tickets.

### 4.4 Ticket Search

```http
GET /api/tickets/search
```

Searches tickets by filters such as sport, city, venue, date, team, and price.

### 4.5 Ticket Detail

```http
GET /api/tickets/{id}
```

Returns detail of one ticket.

---

## 5. Reservation and Payment APIs

Owner: Saghar

### 5.1 Reserve Ticket

```http
POST /api/tickets/{id}/reserve
```

Creates a pending reservation for one ticket.

### 5.2 Pay Reservation

```http
POST /api/reservations/{id}/pay
```

Pays a pending reservation locally.

### 5.3 Cancellation Penalty

```http
GET /api/reservations/{id}/cancellation-penalty
```

Calculates cancellation penalty.

### 5.4 Cancel Reservation

```http
POST /api/reservations/{id}/cancel
```

Cancels reservation and creates refund if payment exists.

---

## 6. Reports and Admin APIs

Owner: Sarina

### 6.1 Create Report

```http
POST /api/reports
```

Creates a user report.

### 6.2 Admin Reports

```http
GET /api/admin/reports
```

Returns reports for support/admin users.

### 6.3 Update Report

```http
PATCH /api/admin/reports/{id}
```

Updates report status.

### 6.4 Suspicious Reservations

```http
GET /api/admin/reservations/suspicious
```

Returns suspicious reservations.

---

## 7. Auth and Security Summary

- Passwords are never stored as plain text.
- Password hashing is implemented in `PasswordHasher`.
- JWT is generated by `JwtUtil`.
- Protected APIs use `AuthMiddleware`.
- Customer APIs only return data for the logged-in user.
- SQL queries use JDBC and PreparedStatement.
- No Spring, ORM, Maven, Gradle, or external heavy package is used.

---

## 8. Route Integration Note

The implementation files are added by each teammate separately.  
`Main.java` and `Router.java` should be integrated at the end by Saghar to avoid conflicts.

Expected route ownership:

| Route | Owner |
|---|---|
| `/api/auth/signup` | Shamim |
| `/api/auth/login` | Shamim |
| `/api/auth/request-otp` | Shamim |
| `/api/auth/verify-otp` | Shamim |
| `/api/users/me` | Shamim |
| `/api/users/me/bookings` | Shamim |
| `/api/tickets/*` | Sarina |
| `/api/cities` | Sarina |
| `/api/venues` | Sarina |
| `/api/reports` | Sarina |
| `/api/admin/*` | Sarina |
| `/api/reservations/*` | Saghar |
| `/api/tickets/{id}/reserve` | Saghar |
