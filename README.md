# Sports Ticket Reservation Database Project

A university database project for a sports match ticket reservation and purchasing system.

The project is implemented with:

```text
Database: PostgreSQL
Backend: Raw Java + JDBC
Frontend: HTML/CSS/Vanilla JavaScript
Search: Elasticsearch with SQL fallback
API format: JSON
```

No ORM, Spring, React, Vue, Tailwind, npm, or heavy package manager is used.

---

## 1. Project Overview

This system supports the main workflow of reserving and buying tickets for sports matches.

Main features:

```text
User signup, login and OTP flow
User profile management
City and venue listing
Ticket search and filtering
Ticket detail view
Ticket reservation
Reservation payment
Booking history
Cancellation penalty check
Reservation cancellation
User report submission
Admin/support report review
Elasticsearch ticket search
SQL fallback search
```

---

## 2. Phase Status

### Phase 1

```text
Project setup
Initial database design
Core table planning
Repository and branch workflow
```

### Phase 2

```text
PostgreSQL schema implementation
Seed data
Core SQL queries
Database constraints and relations
```

### Phase 3

```text
Raw Java backend
JDBC repositories
REST-like JSON APIs
Authentication
Ticket APIs
Reservation APIs
Payment APIs
Report/admin APIs
```

### Phase 4

```text
Frontend UI
Elasticsearch ticket search
SQL fallback
Final route integration
CORS fixes
End-to-end demo flow
Final documentation
```

---

## 3. Repository Structure

```text
backend/
  src/
    Main.java
    config/
    controllers/
    services/
    repositories/
    db/
    http/
    security/
    cache/
    elastic/
  lib/
  out/

frontend/
  index.html
  login.html
  tickets.html
  ticket-detail.html
  profile.html
  bookings.html
  admin.html
  css/
  js/

elastic/
  ticket_index_mapping.json
  sample_elastic_requests.md

docs/
  phase4-elastic.md
  phase4-ui.md
  demo-flow.md
  phase4-report.md
  screenshots/
```

---

## 4. Requirements

Install and prepare:

```text
Java JDK
PostgreSQL
PostgreSQL JDBC driver inside backend/lib
Python, only for serving frontend locally
Elasticsearch, optional for search demo
```

Recommended local ports:

```text
Backend: http://localhost:8080
Frontend: http://localhost:5500
Elasticsearch: http://localhost:9200
PostgreSQL: localhost:5432
```

---

## 5. Configuration

Backend configuration is handled in:

```text
backend/src/config/AppConfig.java
```

Supported environment variables:

```text
SERVER_PORT
DB_DRIVER_CLASS_NAME
DB_URL
DB_USER
DB_PASSWORD
DB_LOGIN_TIMEOUT_SECONDS
JWT_SECRET
JWT_EXPIRATION_MINUTES
OTP_TTL_MINUTES
RESERVATION_TTL_MINUTES
ELASTIC_ENABLED
ELASTIC_URL
ELASTIC_TICKET_INDEX
ELASTIC_TIMEOUT_SECONDS
FRONTEND_DEV_MODE
```

Default values are available in `AppConfig.java`, so local demo can run with minimal configuration if database settings
match the defaults.

---

## 6. Compile Backend

From the project root, run:

```powershell
javac -cp "backend/lib/*" -d backend/out `
  backend/src/Main.java `
  backend/src/config/AppConfig.java `
  backend/src/db/Database.java `
  backend/src/http/JsonResponse.java `
  backend/src/http/RequestUtils.java `
  backend/src/http/Router.java `
  backend/src/security/*.java `
  backend/src/controllers/*.java `
  backend/src/services/*.java `
  backend/src/repositories/*.java `
  backend/src/cache/*.java `
  backend/src/elastic/*.java
```

---

## 7. Run Backend

From the project root, run:

```powershell
java -cp "backend/out;backend/lib/*" Main
```

Expected output:

```text
Sports Ticket Reservation API is running on http://localhost:8080
Health check: http://localhost:8080/api/health
Frontend default URL: http://localhost:5500
Elasticsearch URL: http://localhost:9200
Elasticsearch ticket index: tickets
```

---

## 8. Run Frontend

Open a new terminal:

```powershell
cd frontend
python -m http.server 5500
```

Open in browser:

```text
http://localhost:5500
```

Main frontend pages:

```text
http://localhost:5500/index.html
http://localhost:5500/login.html
http://localhost:5500/tickets.html
http://localhost:5500/ticket-detail.html?id=1
http://localhost:5500/profile.html
http://localhost:5500/bookings.html
http://localhost:5500/admin.html
```

---

## 9. Elasticsearch Setup

Elasticsearch is used for ticket search in phase 4.

Default values:

```text
ELASTIC_ENABLED=true
ELASTIC_URL=http://localhost:9200
ELASTIC_TICKET_INDEX=tickets
```

Mapping file:

```text
elastic/ticket_index_mapping.json
```

Sample requests:

```text
elastic/sample_elastic_requests.md
```

If Elasticsearch is not available, the backend search service should use SQL fallback.

For fallback-only demo:

```text
ELASTIC_ENABLED=false
```

---

## 10. API Health Check

After starting backend:

```powershell
curl.exe -i http://localhost:8080/api/health
```

Expected result:

```text
HTTP/1.1 200 OK
```

Expected JSON:

```json
{
  "status": "UP",
  "service": "sports-ticket-reservation-api",
  "phase": "phase-4-ui-elastic",
  "frontend": "http://localhost:5500",
  "database": "postgresql",
  "search": "elasticsearch-with-sql-fallback"
}
```

---

## 11. CORS Test

The frontend runs on port `5500` and backend runs on port `8080`, so CORS is required.

Test CORS preflight:

```powershell
curl.exe -i -X OPTIONS http://localhost:8080/api/tickets/search
```

Expected result:

```text
HTTP/1.1 204 No Content
Access-control-allow-origin: *
Access-control-allow-methods: GET, POST, PATCH, DELETE, OPTIONS
Access-control-allow-headers: Content-Type, Authorization, Accept
```

---

## 12. Main API Routes

### Base

```text
GET /api/health
```

### Authentication

```text
POST /api/auth/signup
POST /api/auth/request-otp
POST /api/auth/verify-otp
POST /api/auth/login
```

### User

```text
GET /api/users/me
PATCH /api/users/me
GET /api/users/me/bookings
```

### Tickets

```text
GET /api/cities
GET /api/venues
GET /api/tickets
GET /api/tickets/search
GET /api/tickets/{id}
```

### Reservation and Payment

```text
POST /api/tickets/{id}/reserve
POST /api/reservations/{id}/pay
GET /api/reservations/{id}/cancellation-penalty
POST /api/reservations/{id}/cancel
```

### Reports and Admin

```text
POST /api/reports
GET /api/reports/me
GET /api/admin/reports
PATCH /api/admin/reports/{id}
GET /api/admin/reservations/suspicious
PATCH /api/admin/reservations/{id}
```

---

## 13. Final Demo Flow

Recommended final demo order:

```text
1. Start PostgreSQL
2. Start Elasticsearch if available
3. Compile backend
4. Run backend
5. Run frontend static server
6. Open home page
7. Test health check
8. Login or use mock login
9. Search tickets
10. Apply filters
11. Open ticket detail
12. Reserve ticket
13. Pay reservation
14. Open profile
15. Open bookings
16. Check cancellation penalty
17. Cancel reservation
18. Submit report
19. Open admin page
20. Review/update report
21. Explain Elasticsearch and SQL fallback
```

Detailed demo document:

```text
docs/demo-flow.md
```

---

## 14. Phase 4 Documentation

Phase 4 documentation files:

```text
docs/phase4-elastic.md
docs/phase4-ui.md
docs/demo-flow.md
docs/phase4-report.md
```

Recommended screenshot folder:

```text
docs/screenshots/phase4/
```

---

## 15. Final Phase 4 Checklist

Before final PR:

```text
Backend compiles successfully
Backend starts successfully
GET /api/health returns 200
OPTIONS /api/tickets/search returns 204
Frontend starts on localhost:5500
Home page opens
Login page opens
Ticket search page opens
Ticket detail page opens
Profile page opens
Bookings page opens
Admin page opens
Ticket search works with Elasticsearch or SQL fallback
Demo flow is documented
Phase 4 report is documented
README is updated
```

---

## 16. Branch Workflow

Phase 4 main branch:

```text
phase-4-ui-elastic
```

Personal branches:

```text
sarina/phase4-elastic-search
shamim/phase4-frontend-ui
saghar/phase4-integration-polish
```

PR order:

```text
1. sarina/phase4-elastic-search -> phase-4-ui-elastic
2. shamim/phase4-frontend-ui -> phase-4-ui-elastic
3. saghar/phase4-integration-polish -> phase-4-ui-elastic
4. phase-4-ui-elastic -> develop
5. develop -> main
```

---

## 17. Notes

This project is designed as a database course project and focuses on:

```text
Database-backed application design
SQL and relational schema usage
Raw JDBC integration
REST-like API design
Frontend/API interaction
Search integration
Final documentation and demo readiness
```

The frontend is intentionally simple and dependency-free to match the project constraints.
