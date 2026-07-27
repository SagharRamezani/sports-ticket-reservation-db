# Phase 4 - Frontend UI Documentation

Project: Sports Match Ticket Reservation System  
Owner: Shamim  
Scope: HTML/CSS/Vanilla JavaScript frontend pages and API usage

---

## 1. Goal

The goal of the frontend part of phase 4 is to provide a simple, clean, and usable UI for testing the backend APIs implemented in phase 3 and the Elasticsearch search integration implemented in phase 4.

The frontend is intentionally lightweight:

```text
HTML
CSS
Vanilla JavaScript
No React
No Vue
No Angular
No npm
No package manager
```

The frontend can run by opening HTML files directly or by using a simple local server.

Recommended local server:

```bash
cd frontend
python -m http.server 5500
```

Then open:

```text
http://localhost:5500
```

---

## 2. Frontend Folder Structure

Frontend files are placed under:

```text
frontend/
```

Main pages:

```text
frontend/index.html
frontend/login.html
frontend/tickets.html
frontend/ticket-detail.html
frontend/profile.html
frontend/bookings.html
frontend/admin.html
```

Shared styles:

```text
frontend/css/style.css
```

JavaScript files:

```text
frontend/js/api.js
frontend/js/auth.js
frontend/js/tickets.js
frontend/js/reservations.js
frontend/js/profile.js
frontend/js/admin.js
frontend/js/mock-data.js
```

---

## 3. API Access Rule

All backend requests should go through:

```text
frontend/js/api.js
```

This file defines the shared `ApiClient` object.

The goal is to keep backend communication in one central place.

Main helper methods:

```text
ApiClient.get(path, options)
ApiClient.post(path, body, options)
ApiClient.patch(path, body, options)
ApiClient.delete(path, options)
ApiClient.buildQuery(params)
ApiClient.getToken()
ApiClient.setToken(token)
ApiClient.clearToken()
ApiClient.setCurrentUser(user)
ApiClient.getCurrentUser()
```

Default backend URL:

```text
http://localhost:8080
```

The frontend stores auth token and user data in `localStorage`.

---

## 4. Mock Data Strategy

If backend is unavailable, some pages can still be tested using mock data.

Mock data file:

```text
frontend/js/mock-data.js
```

The mock fallback is useful for:

```text
Screenshots
UI demo
Frontend-only testing
Backend downtime
CORS issue debugging
```

Important note:

```text
Mock data is only for frontend demo and should not replace real backend testing.
```

---

## 5. Page: Home

File:

```text
frontend/index.html
```

Purpose:

```text
Landing page for phase 4 frontend demo
Navigation to all main pages
Health check test for backend
Overview of frontend demo flow
```

Related API:

```text
GET /api/health
```

Main UI elements:

```text
Header navigation
Hero section
Feature cards
API base URL display
Health check button
Footer
```

---

## 6. Page: Login / Signup / OTP

Files:

```text
frontend/login.html
frontend/js/auth.js
```

Purpose:

```text
User login
User signup
OTP request
OTP verification
Local session/token test
Mock login for frontend demo
```

Related APIs:

```text
POST /api/auth/login
POST /api/auth/signup
POST /api/auth/request-otp
POST /api/auth/verify-otp
```

Stored data:

```text
sports_ticket_auth_token
sports_ticket_current_user
```

Notes:

```text
If backend returns token, it is saved in localStorage.
If backend is unavailable, mock login can be used for UI screenshots.
```

---

## 7. Page: Ticket Search

Files:

```text
frontend/tickets.html
frontend/js/tickets.js
frontend/js/mock-data.js
```

Purpose:

```text
Search available tickets
Filter tickets
Show search results as cards
Open ticket detail page
Test Elasticsearch-backed search endpoint
```

Related API:

```text
GET /api/tickets/search
```

Supported filters:

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

Example request:

```text
GET /api/tickets/search?sport=Football&city=Tehran&category=VIP
```

Expected response fields used by UI:

```text
success
searchEngine
fallbackUsed
filters
data
count
```

Notes:

```text
searchEngine and fallbackUsed are displayed to show whether Elasticsearch or SQL fallback was used.
```

---

## 8. Page: Ticket Detail + Reservation + Payment + Report

Files:

```text
frontend/ticket-detail.html
frontend/js/reservations.js
```

Purpose:

```text
Show selected ticket details
Reserve ticket
Pay reservation
Submit report for a ticket/reservation problem
```

The page reads ticket id from URL:

```text
ticket-detail.html?id=1
```

Related APIs:

```text
GET /api/tickets/{id}
POST /api/tickets/{id}/reserve
POST /api/reservations/{id}/pay
POST /api/reports
```

Main UI sections:

```text
Ticket detail card
Reservation panel
Payment panel
Report problem form
```

Notes:

```text
Reservation ID returned by the reserve API is automatically placed in payment and report fields when possible.
```

---

## 9. Page: Profile

Files:

```text
frontend/profile.html
frontend/js/profile.js
```

Purpose:

```text
Load current user profile
Update user profile
Display local auth/session state
```

Related APIs:

```text
GET /api/users/me
PATCH /api/users/me
```

Profile fields used by UI:

```text
firstName
lastName
email
phoneNumber
cityId
profileImageUrl
```

Notes:

```text
If profile API fails, localStorage or mock profile is shown for demo.
```

---

## 10. Page: Bookings

Files:

```text
frontend/bookings.html
frontend/js/reservations.js
```

Purpose:

```text
Show booking history
Pay pending reservations
Check cancellation penalty
Cancel reservations
```

Related APIs:

```text
GET /api/users/me/bookings
POST /api/reservations/{id}/pay
GET /api/reservations/{id}/cancellation-penalty
POST /api/reservations/{id}/cancel
```

Main UI elements:

```text
Booking cards
Pay button
Penalty button
Cancel button
Manual reservation action form
```

Notes:

```text
This page reuses reservations.js because payment and cancellation logic are related to reservation flow.
```

---

## 11. Page: Admin / Support

Files:

```text
frontend/admin.html
frontend/js/admin.js
```

Purpose:

```text
Load user reports
Display reports as admin cards
Update report status
Mark reports as resolved
```

Related APIs:

```text
GET /api/admin/reports
PATCH /api/admin/reports/{id}
```

Supported report statuses in UI:

```text
OPEN
IN_PROGRESS
RESOLVED
REJECTED
```

Notes:

```text
If admin authorization fails, login with an admin/support account.
If backend is unavailable, mock reports can be shown for screenshots.
```

---

## 12. Responsive Design

Responsive design is handled in:

```text
frontend/css/style.css
```

The UI uses:

```text
Flexible containers
Grid layout
Responsive cards
Mobile-friendly form layout
Sticky header
Reusable buttons and panels
```

Important breakpoints:

```text
920px
620px
```

At smaller screen sizes:

```text
Navigation wraps
Cards become one-column or two-column
Forms become one-column
Buttons become full-width where needed
```

---

## 13. Test Plan

### 13.1 Run Frontend

```bash
cd frontend
python -m http.server 5500
```

Open:

```text
http://localhost:5500
```

### 13.2 Test Home

```text
Open index.html
Click health check
Check if backend response appears
```

### 13.3 Test Auth

```text
Open login.html
Try login/signup/OTP forms
Check localStorage token
Try mock login
```

### 13.4 Test Ticket Search

```text
Open tickets.html
Search by sport/city/category
Check result cards
Open ticket detail from a result
```

### 13.5 Test Ticket Detail

```text
Open ticket-detail.html?id=1
Check ticket information
Click reserve
Click pay
Submit report
```

### 13.6 Test Profile

```text
Open profile.html
Load profile
Edit fields
Submit update
Check session box
```

### 13.7 Test Bookings

```text
Open bookings.html
Load bookings
Pay reservation
Check penalty
Cancel reservation
```

### 13.8 Test Admin

```text
Open admin.html
Load reports
Edit a report
Mark report as resolved
```

---

## 14. Known Integration Notes for Saghar

The frontend expects these backend routes to be available:

```text
GET /api/health
POST /api/auth/signup
POST /api/auth/request-otp
POST /api/auth/verify-otp
POST /api/auth/login
GET /api/users/me
PATCH /api/users/me
GET /api/users/me/bookings
GET /api/tickets/search
GET /api/tickets/{id}
POST /api/tickets/{id}/reserve
POST /api/reservations/{id}/pay
GET /api/reservations/{id}/cancellation-penalty
POST /api/reservations/{id}/cancel
POST /api/reports
GET /api/admin/reports
PATCH /api/admin/reports/{id}
```

If any page fails because of CORS, route mismatch, response field mismatch, or auth middleware issue, it should be handled in Saghar's integration branch.

Do not change Sarina's Elasticsearch code from the frontend branch.

Do not change `Main.java` or `Router.java` from Shamim's branch.

---

## 15. Screenshot Checklist

Recommended screenshots for the final report:

```text
Home page
Login/signup/OTP page
Ticket search page with results
Ticket detail page
Reservation/payment section
Profile page
Bookings page
Admin reports page
Backend health check result
Search response showing searchEngine/fallbackUsed
```

Suggested screenshot folder:

```text
docs/screenshots/phase4/
```

Screenshots can be added later during Saghar's final integration/report step if the team wants to keep Shamim's branch focused on UI code and UI documentation.

---

## 16. Commit Summary

Frontend commits by Shamim:

```text
feat: add shared frontend layout and api helper
feat: implement login signup and otp UI
feat: implement ticket search UI with filters
feat: implement ticket detail and reservation UI
feat: implement profile bookings payment and cancellation UI
feat: implement admin report dashboard UI
docs: document phase 4 frontend pages and api usage
```

---

## 17. Final Summary

The frontend provides a simple web interface for the main phase 3 and phase 4 APIs.

It supports:

```text
Authentication
Ticket search
Ticket detail
Reservation
Payment
Cancellation
Profile
Bookings
Report submission
Admin report management
Mock fallback for demo
Responsive layout
```

This UI is ready for Saghar's final integration, bug fixing, screenshots, and phase 4 report.
