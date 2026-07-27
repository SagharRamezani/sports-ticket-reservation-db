# Phase 4 Final Demo Flow

Project: Sports Match Ticket Reservation System  
Phase: 4 - UI + Elasticsearch + Final Integration  
Owner: Saghar  
Database: PostgreSQL  
Backend: Raw Java + JDBC  
Frontend: HTML/CSS/Vanilla JavaScript  
Search: Elasticsearch with SQL fallback

---

## 1. Demo Goal

The goal of this demo is to show the final integrated flow of the Sports Ticket Reservation System after completing
phases 1, 2, 3, and 4.

This demo proves that:

```text
Backend APIs are registered and reachable
Frontend can communicate with backend
CORS works correctly
Ticket search works through Elasticsearch or SQL fallback
Reservation and payment flow can be tested end-to-end
User profile and booking history pages are connected
Report submission and admin review flow are available
```

---

## 2. Required Services Before Demo

Before starting the demo, these services should be ready:

```text
PostgreSQL database
Java backend server
Frontend static server
Elasticsearch service, if available
```

If Elasticsearch is not available, the system should continue with SQL fallback for ticket search.

---

## 3. Recommended Terminal Setup

Use three terminals.

### Terminal 1 - Backend

Compile the backend:

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

Run the backend:

```powershell
java -cp "backend/out;backend/lib/*" Main
```

Expected backend output:

```text
Sports Ticket Reservation API is running on http://localhost:8080
Health check: http://localhost:8080/api/health
Frontend default URL: http://localhost:5500
Elasticsearch URL: http://localhost:9200
Elasticsearch ticket index: tickets
```

---

### Terminal 2 - Frontend

Run the frontend static server:

```powershell
cd frontend
python -m http.server 5500
```

Open in browser:

```text
http://localhost:5500
```

---

### Terminal 3 - Quick API Tests

Health check:

```powershell
curl.exe -i http://localhost:8080/api/health
```

CORS preflight test:

```powershell
curl.exe -i -X OPTIONS http://localhost:8080/api/tickets/search
```

Expected CORS result:

```text
HTTP/1.1 204 No Content
Access-control-allow-origin: *
Access-control-allow-methods: GET, POST, PATCH, DELETE, OPTIONS
Access-control-allow-headers: Content-Type, Authorization, Accept
```

---

## 4. Demo Step 1 - Open Home Page

Open:

```text
http://localhost:5500/index.html
```

Show:

```text
Home page
Main navigation
Backend health check section
Links to Tickets, Login, Bookings, Profile and Admin pages
```

Click the health check button.

Expected result:

```text
Backend returns status UP
Phase is phase-4-ui-elastic
Search is elasticsearch-with-sql-fallback
```

---

## 5. Demo Step 2 - Authentication Page

Open:

```text
http://localhost:5500/login.html
```

Show these sections:

```text
Login
Signup
Request OTP
Verify OTP
Current session
```

Recommended demo action:

```text
Use login with a demo user, or use mock login if backend auth data is not ready.
```

If real backend data is ready, test:

```text
POST /api/auth/login
```

Expected result:

```text
Token is saved in localStorage
Current session box shows user/token data
Frontend can use token for protected APIs
```

---

## 6. Demo Step 3 - Ticket Search

Open:

```text
http://localhost:5500/tickets.html
```

Show search filters:

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

Test a basic search:

```text
Click Search
```

Related backend API:

```text
GET /api/tickets/search
```

Expected result:

```text
Ticket cards are shown
Each result contains ticket/match/venue/price/status information
Search metadata shows searchEngine and fallbackUsed when backend provides them
```

Important explanation for presentation:

```text
If Elasticsearch is available, the search result should come from Elasticsearch.
If Elasticsearch is unavailable, backend SearchService falls back to SQL search.
```

---

## 7. Demo Step 4 - Filtered Ticket Search

Try filters one by one:

```text
Filter by sport
Filter by city
Filter by ticket category
Filter by min/max price
Filter by match date
```

Expected result:

```text
The result list changes based on filters
The frontend sends query parameters to /api/tickets/search
```

Example:

```text
/api/tickets/search?sport=Football&city=Tehran&category=VIP
```

---

## 8. Demo Step 5 - Ticket Detail

From the ticket search page, click a ticket detail link.

Or open directly:

```text
http://localhost:5500/ticket-detail.html?id=1
```

Related backend API:

```text
GET /api/tickets/{id}
```

Show:

```text
Ticket ID
Seat
Sport
Category
Venue
City
Teams
Match time
Ticket status
Price
```

Expected result:

```text
The selected ticket information is displayed.
```

---

## 9. Demo Step 6 - Reserve Ticket

On ticket detail page, click:

```text
Reserve ticket
```

Related backend API:

```text
POST /api/tickets/{id}/reserve
```

Expected result:

```text
Reservation is created
Reservation ID is shown in the reservation result panel
Reservation ID is automatically copied to payment and report fields when possible
```

Presentation note:

```text
Reservation should have a limited TTL based on backend reservation config.
```

---

## 10. Demo Step 7 - Pay Reservation

On the same page, fill or confirm:

```text
Reservation ID
Payment method
Tracking code
Amount
```

Click:

```text
Pay reservation
```

Related backend API:

```text
POST /api/reservations/{id}/pay
```

Expected result:

```text
Payment response is shown
Reservation status becomes paid/confirmed if backend data allows it
Ticket status is updated by backend if implemented
```

---

## 11. Demo Step 8 - User Profile

Open:

```text
http://localhost:5500/profile.html
```

Related APIs:

```text
GET /api/users/me
PATCH /api/users/me
```

Show:

```text
Profile fields
Current local session
API response panel
```

Demo actions:

```text
Load profile
Edit one simple field
Submit update
```

Expected result:

```text
Profile response is shown
Updated data is reflected in UI or local session
```

---

## 12. Demo Step 9 - Booking History

Open:

```text
http://localhost:5500/bookings.html
```

Related API:

```text
GET /api/users/me/bookings
```

Show:

```text
Booking history cards
Reservation ID
Reservation status
Seat
Amount
Match title
Match time
```

Expected result:

```text
User bookings are displayed.
```

---

## 13. Demo Step 10 - Cancellation Penalty

On bookings page, choose a reservation and click:

```text
Penalty
```

Related backend API:

```text
GET /api/reservations/{id}/cancellation-penalty
```

Expected result:

```text
Penalty response is shown in action result box.
```

Explain:

```text
The backend calculates or returns the penalty information before cancellation.
```

---

## 14. Demo Step 11 - Cancel Reservation

On bookings page, choose a reservation and click:

```text
Cancel
```

Or use manual action form:

```text
Reservation ID
Cancel reason
Cancel reservation
```

Related backend API:

```text
POST /api/reservations/{id}/cancel
```

Expected result:

```text
Cancellation response is shown
Booking list refreshes
Reservation status becomes cancelled if backend data allows it
```

---

## 15. Demo Step 12 - Submit Report

Open ticket detail page:

```text
http://localhost:5500/ticket-detail.html?id=1
```

Scroll to:

```text
Report Problem
```

Fill:

```text
Title
Category
Reservation ID
Ticket ID
Description
```

Related backend API:

```text
POST /api/reports
```

Expected result:

```text
Report is submitted
Report response is shown in the report result box
```

---

## 16. Demo Step 13 - Admin Review

Open:

```text
http://localhost:5500/admin.html
```

Related APIs:

```text
GET /api/admin/reports
PATCH /api/admin/reports/{id}
```

Show:

```text
Admin/support dashboard
Report cards
Report ID
Status
Category
Reporter
Ticket ID
Reservation ID
Created date
```

Demo actions:

```text
Load reports
Click Edit on a report
Change status to IN_PROGRESS or RESOLVED
Submit update
Click Mark resolved
```

Expected result:

```text
Admin report status update response is shown
Reports can be reviewed from the frontend
```

---

## 17. Demo Step 14 - Elasticsearch / SQL Fallback

Open:

```text
http://localhost:5500/tickets.html
```

Run a ticket search.

Explain:

```text
The frontend calls /api/tickets/search.
The backend SearchService first tries Elasticsearch if enabled and reachable.
If Elasticsearch fails or is disabled, SQL fallback is used.
The UI can show searchEngine and fallbackUsed fields when backend returns them.
```

Recommended backend environment variables:

```text
ELASTIC_ENABLED=true
ELASTIC_URL=http://localhost:9200
ELASTIC_TICKET_INDEX=tickets
```

For fallback-only demo:

```text
ELASTIC_ENABLED=false
```

Expected result:

```text
Search still works even when Elasticsearch is not available.
```

---

## 18. Final Screenshot Checklist

Take screenshots from these pages:

```text
1. Home page
2. Health check result
3. Login/signup/OTP page
4. Ticket search with results
5. Ticket search with filters
6. Ticket detail page
7. Reservation result
8. Payment result
9. Profile page
10. Bookings page
11. Cancellation penalty result
12. Cancel reservation result
13. Report submission result
14. Admin reports dashboard
15. Admin report update result
```

Suggested folder:

```text
docs/screenshots/phase4/
```

---

## 19. Known Demo Risks

Possible issues and handling:

```text
If backend is not running:
  Start backend with java -cp "backend/out;backend/lib/*" Main

If CORS fails:
  Check JsonResponse.addCorsHeaders and OPTIONS route handling

If frontend cannot call backend:
  Check ApiClient base URL: http://localhost:8080

If Elasticsearch is down:
  Use SQL fallback and explain fallback strategy

If auth-protected endpoints fail:
  Login first or use seeded/demo users

If a route returns 404:
  Check Main.java route registration and Router dynamic route matching
```

---

## 20. Final Demo Order Summary

Use this order in presentation:

```text
1. Start backend
2. Start frontend
3. Health check
4. Login/signup
5. Ticket search
6. Filter search
7. Ticket detail
8. Reserve ticket
9. Pay reservation
10. Profile
11. Bookings
12. Cancellation penalty
13. Cancel reservation
14. Submit report
15. Admin review
16. Explain Elasticsearch + SQL fallback
17. Show docs and README
```

---

## 21. Final Notes

This demo flow is designed for the final phase 4 presentation and VC review.

It focuses on:

```text
Integration
Usable UI
Connected APIs
Search behavior
CORS correctness
End-to-end business flow
Documentation readiness
```

The frontend is intentionally simple and dependency-free, matching the project constraints.
