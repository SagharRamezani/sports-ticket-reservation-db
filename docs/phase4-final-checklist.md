# Phase 4 Final Checklist

Project: Sports Match Ticket Reservation System  
Phase: 4 - Final Integration  
Owner: Saghar

---

## 1. Branch Checklist

Before opening the final PR, check that the current branch is:

```text
saghar/phase4-integration-polish
```

Commands:

```bash
git branch
git status
git log --oneline -10
```

Expected condition:

```text
Working tree is clean
All Saghar phase 4 commits are pushed
```

---

## 2. Required Phase 4 Documents

These documents should exist:

```text
docs/phase4-elastic.md
docs/phase4-ui.md
docs/demo-flow.md
docs/phase4-report.md
docs/phase4-final-checklist.md
README.md
```

---

## 3. Required Phase 4 Backend Files

Check that these backend files exist and compile:

```text
backend/src/Main.java
backend/src/http/Router.java
backend/src/http/JsonResponse.java
backend/src/config/AppConfig.java
backend/src/elastic/ElasticTicketSearch.java
backend/src/services/SearchService.java
```

---

## 4. Required Phase 4 Frontend Files

Check that these frontend files exist:

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
```

---

## 5. Compile Test

Run from project root:

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

Expected result:

```text
No compile errors
```

---

## 6. Backend Run Test

Run:

```powershell
java -cp "backend/out;backend/lib/*" Main
```

Expected output:

```text
Sports Ticket Reservation API is running on http://localhost:8080
Health check: http://localhost:8080/api/health
Frontend default URL: http://localhost:5500
```

---

## 7. API Health Test

Run in another terminal:

```powershell
curl.exe -i http://localhost:8080/api/health
```

Expected result:

```text
HTTP/1.1 200 OK
```

Expected JSON fields:

```text
status: UP
service: sports-ticket-reservation-api
phase: phase-4-ui-elastic
frontend: http://localhost:5500
database: postgresql
search: elasticsearch-with-sql-fallback
```

---

## 8. CORS Test

Run:

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

## 9. Frontend Run Test

Run:

```powershell
cd frontend
python -m http.server 5500
```

Open:

```text
http://localhost:5500
```

Expected result:

```text
Frontend opens without package installation
Navigation links work
No npm or frontend framework is required
```

---

## 10. Frontend Page Checklist

Open and test:

```text
http://localhost:5500/index.html
http://localhost:5500/login.html
http://localhost:5500/tickets.html
http://localhost:5500/ticket-detail.html?id=1
http://localhost:5500/profile.html
http://localhost:5500/bookings.html
http://localhost:5500/admin.html
```

Expected result:

```text
Each page loads
Shared CSS is applied
Navigation is visible
API result boxes are visible where needed
```

---

## 11. End-to-End Demo Checklist

Test this order:

```text
1. Home page opens
2. Health check works
3. Login or mock login works
4. Ticket search works
5. Filters are sent to backend
6. Ticket detail opens
7. Reserve ticket action is available
8. Payment action is available
9. Profile page loads
10. Bookings page loads
11. Cancellation penalty action is available
12. Cancel reservation action is available
13. Report submission is available
14. Admin report review is available
15. Search works with Elasticsearch or SQL fallback
```

---

## 12. Screenshot Checklist

Save final screenshots in:

```text
docs/screenshots/phase4/
```

Recommended screenshots:

```text
01-home.png
02-health-check.png
03-login.png
04-ticket-search.png
05-filtered-search.png
06-ticket-detail.png
07-reservation-result.png
08-payment-result.png
09-profile.png
10-bookings.png
11-cancellation-penalty.png
12-cancel-reservation.png
13-report-submit.png
14-admin-reports.png
15-admin-update.png
```

---

## 13. Final PR Checklist

Before PR:

```text
git status is clean
backend compiles
backend runs
frontend runs
health check returns 200
CORS preflight returns 204
demo-flow.md exists
phase4-report.md exists
README.md is updated
screenshot folder exists
```

PR target:

```text
base: phase-4-ui-elastic
compare: saghar/phase4-integration-polish
```

Suggested PR title:

```text
docs: finalize phase 4 integration and demo readiness
```

Suggested PR description:

```text
Final Saghar integration branch for phase 4.

Changes:
- Integrated phase 4 routes and configuration
- Fixed CORS and JSON response handling for frontend
- Added final demo flow
- Added phase 4 implementation report
- Updated README with phase 4 run instructions
- Added final checklist and screenshot folder for presentation readiness

Validation:
- Backend compile checked
- Backend startup checked
- GET /api/health checked
- OPTIONS /api/tickets/search CORS preflight checked
- Frontend static server run instructions documented
```

---

## 14. Final Merge Order

Use this order:

```text
1. saghar/phase4-integration-polish -> phase-4-ui-elastic
2. phase-4-ui-elastic -> develop
3. develop -> main
```
