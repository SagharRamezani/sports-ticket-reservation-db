# Phase 4 Implementation Report

Project: Sports Match Ticket Reservation System  
Course: Database Project  
Phase: 4 - UI, Elasticsearch Search, Final Integration  
Database: PostgreSQL  
Backend: Raw Java + JDBC  
Frontend: HTML/CSS/Vanilla JavaScript  
Search Engine: Elasticsearch with SQL fallback

---

## 1. Phase 4 Overview

In phase 4, the project was extended from a backend API system into an integrated demo-ready system.

The main focus of this phase was:

```text
1. Adding a simple frontend UI
2. Connecting the UI to the phase 3 backend APIs
3. Adding Elasticsearch for ticket search
4. Keeping SQL search as a fallback
5. Final integration and route/CORS fixes
6. Preparing documentation and demo flow
```

The project constraints were respected:

```text
No ORM
No Spring
No React
No Vue
No Tailwind
No npm/package manager
No heavy frontend dependencies
```

---

## 2. Team Work Distribution

### Sarina - Elasticsearch and Search Backend

Sarina worked on:

```text
Elasticsearch index mapping
Sample Elasticsearch requests
ElasticTicketSearch client
SearchService integration
SQL fallback for search
Elasticsearch documentation
```

Main files:

```text
elastic/ticket_index_mapping.json
elastic/sample_elastic_requests.md
backend/src/elastic/ElasticTicketSearch.java
backend/src/services/SearchService.java
backend/src/repositories/TicketRepository.java
backend/src/controllers/TicketController.java
docs/phase4-elastic.md
```

---

### Shamim - Frontend UI

Shamim worked on:

```text
Shared frontend layout
API helper
Authentication UI
Ticket search UI
Ticket detail page
Reservation and payment UI
Profile page
Bookings page
Admin/support report dashboard
Frontend UI documentation
```

Main files:

```text
frontend/index.html
frontend/login.html
frontend/tickets.html
frontend/ticket-detail.html
frontend/profile.html
frontend/bookings.html
frontend/admin.html
frontend/css/style.css
frontend/js/api.js
frontend/js/auth.js
frontend/js/tickets.js
frontend/js/reservations.js
frontend/js/profile.js
frontend/js/admin.js
frontend/js/mock-data.js
docs/phase4-ui.md
```

---

### Saghar - Final Integration and Report

Saghar worked on:

```text
Merging Sarina and Shamim branches
Registering final backend routes
Fixing dynamic route handling
Fixing CORS for frontend
Checking JSON response format
Testing end-to-end demo flow
Writing demo-flow.md
Writing phase4-report.md
Updating README
Preparing final PR
```

Main files:

```text
backend/src/Main.java
backend/src/http/Router.java
backend/src/config/AppConfig.java
backend/src/http/JsonResponse.java
docs/demo-flow.md
docs/phase4-report.md
README.md
```

---

## 3. Backend Integration

The backend is implemented using raw Java and the built-in HTTP server.

The main entry point is:

```text
backend/src/Main.java
```

In phase 4, backend routes were organized into separate registration methods:

```text
registerBaseRoutes
registerAuthRoutes
registerUserRoutes
registerTicketRoutes
registerReservationAndPaymentRoutes
registerReportAndAdminRoutes
```

This made the final route list easier to review and debug.

---

## 4. Registered Backend Routes

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

### Ticket and Search

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

## 5. Router Improvements

The router supports:

```text
GET
POST
PATCH
DELETE
OPTIONS
```

It also supports dynamic route patterns such as:

```text
/api/tickets/{id}
/api/reservations/{id}/pay
/api/admin/reports/{id}
```

Dynamic matching is handled by comparing the request path with registered route patterns and accepting path parts
wrapped with `{}` as variables.

---

## 6. CORS Fix

The frontend runs on:

```text
http://localhost:5500
```

The backend runs on:

```text
http://localhost:8080
```

Because frontend and backend use different ports, CORS headers were required.

CORS headers are added in:

```text
backend/src/http/JsonResponse.java
```

Headers added:

```text
Access-Control-Allow-Origin
Access-Control-Allow-Methods
Access-Control-Allow-Headers
Access-Control-Max-Age
```

Preflight requests are handled through:

```text
OPTIONS
```

Expected preflight result:

```text
HTTP/1.1 204 No Content
```

---

## 7. JSON Response Standardization

The backend uses JSON responses for API communication.

The response helper is:

```text
backend/src/http/JsonResponse.java
```

Supported helper methods include:

```text
ok
created
noContent
badRequest
unauthorized
forbidden
notFound
conflict
serverError
sendJson
successJson
errorJson
escape
```

The `escape` helper remains public because existing backend services use it to safely build JSON strings.

---

## 8. Configuration

The project configuration is centralized in:

```text
backend/src/config/AppConfig.java
```

It supports environment variables and fallback default values.

Important configuration values:

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

Default server URL:

```text
http://localhost:8080
```

Default frontend URL:

```text
http://localhost:5500
```

Default Elasticsearch URL:

```text
http://localhost:9200
```

---

## 9. Elasticsearch Search

Phase 4 adds Elasticsearch search for tickets.

Main goal:

```text
Improve ticket search and filtering
Support better matching for sport, city, venue, team, category and match data
Keep SQL fallback if Elasticsearch is unavailable
```

Search endpoint:

```text
GET /api/tickets/search
```

Frontend page:

```text
frontend/tickets.html
```

Search parameters:

```text
sport
city
venue
team
category
min_price
max_price
match_date
```

Search behavior:

```text
1. Frontend sends query parameters to /api/tickets/search
2. Backend SearchService tries Elasticsearch first
3. If Elasticsearch fails or is disabled, backend uses SQL fallback
4. Response is returned as JSON
5. UI displays ticket cards and search metadata
```

---

## 10. SQL Fallback Strategy

SQL fallback is important because the demo should still work even if Elasticsearch is not running.

Fallback is used when:

```text
Elasticsearch is disabled
Elasticsearch is unreachable
Elasticsearch request fails
Index is missing
Search throws an exception
```

This improves reliability for presentation and VC review.

---

## 11. Frontend Integration

The frontend is dependency-free and uses:

```text
HTML
CSS
Vanilla JavaScript
```

The frontend is served locally with:

```powershell
cd frontend
python -m http.server 5500
```

Main frontend API helper:

```text
frontend/js/api.js
```

The API helper handles:

```text
Base URL
JSON request body
Authorization token
GET/POST/PATCH/DELETE requests
Query string building
Error handling
Mock fallback
Local storage auth state
```

---

## 12. Frontend Pages

### Home

```text
frontend/index.html
```

Purpose:

```text
Landing page
Navigation
Health check
Phase 4 overview
```

### Login

```text
frontend/login.html
frontend/js/auth.js
```

Purpose:

```text
Signup
Login
Request OTP
Verify OTP
Mock login for demo
```

### Ticket Search

```text
frontend/tickets.html
frontend/js/tickets.js
```

Purpose:

```text
Search tickets
Filter tickets
View ticket cards
Open ticket detail
```

### Ticket Detail

```text
frontend/ticket-detail.html
frontend/js/reservations.js
```

Purpose:

```text
View ticket detail
Reserve ticket
Pay reservation
Submit report
```

### Profile

```text
frontend/profile.html
frontend/js/profile.js
```

Purpose:

```text
View user profile
Update profile
Check local session
```

### Bookings

```text
frontend/bookings.html
frontend/js/reservations.js
```

Purpose:

```text
View booking history
Pay reservation
Check cancellation penalty
Cancel reservation
```

### Admin

```text
frontend/admin.html
frontend/js/admin.js
```

Purpose:

```text
View reports
Update report status
Mark report as resolved
```

---

## 13. End-to-End Demo Flow

The final demo flow is:

```text
1. Start PostgreSQL
2. Start Elasticsearch, if available
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

The detailed demo plan is documented in:

```text
docs/demo-flow.md
```

---

## 14. Testing Results

The following integration tests were checked:

```text
Backend compile
Backend startup
GET /api/health
OPTIONS /api/tickets/search
Frontend static server
Frontend page navigation
CORS response headers
JSON response headers
Dynamic route registration
```

Verified sample health check:

```text
GET /api/health -> 200 OK
```

Verified sample CORS preflight:

```text
OPTIONS /api/tickets/search -> 204 No Content
```

---

## 15. Compile Command

Backend compile command:

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

Run command:

```powershell
java -cp "backend/out;backend/lib/*" Main
```

---

## 16. Screenshot Checklist

Recommended screenshots for final report and presentation:

```text
Home page
Health check result
Login/signup/OTP page
Ticket search page
Filtered ticket search
Ticket detail page
Reservation result
Payment result
Profile page
Bookings page
Cancellation penalty result
Cancel reservation result
Report submission result
Admin report dashboard
Admin report update result
```

Suggested folder:

```text
docs/screenshots/phase4/
```

---

## 17. Known Limitations

Current limitations:

```text
Frontend is intentionally simple and not a production UI
Mock fallback is only for demo/testing
No frontend framework is used
Elasticsearch sync strategy is documented, but full production sync can be improved later
Some endpoints may require seeded users and valid auth token
Local demo depends on PostgreSQL data being available
```

---

## 18. Final Phase 4 Files

Important phase 4 files:

```text
elastic/ticket_index_mapping.json
elastic/sample_elastic_requests.md
backend/src/elastic/ElasticTicketSearch.java
backend/src/services/SearchService.java
frontend/index.html
frontend/login.html
frontend/tickets.html
frontend/ticket-detail.html
frontend/profile.html
frontend/bookings.html
frontend/admin.html
frontend/css/style.css
frontend/js/api.js
frontend/js/auth.js
frontend/js/tickets.js
frontend/js/reservations.js
frontend/js/profile.js
frontend/js/admin.js
frontend/js/mock-data.js
docs/phase4-elastic.md
docs/phase4-ui.md
docs/demo-flow.md
docs/phase4-report.md
README.md
```

---

## 19. Conclusion

Phase 4 completed the final integration of the project.

The system now has:

```text
A connected frontend UI
Backend route integration
CORS support
JSON response handling
Elasticsearch ticket search
SQL fallback search
Reservation/payment/cancellation demo flow
Report/admin review demo flow
Final documentation
```

The project is now ready for final demo, screenshots, PR review, and merge into the main project branch.
