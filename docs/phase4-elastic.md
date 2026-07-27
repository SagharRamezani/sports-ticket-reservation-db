# Phase 4 - Elasticsearch Integration

Project: Sports Match Ticket Reservation System  
Owner: Sarina  
Scope: Elasticsearch ticket search, SQL fallback, and sync strategy

---

## 1. Goal

In phase 4, Elasticsearch is added only for the ticket search API.

The main goals are:

1. Improve ticket search performance.
2. Support text search over sport, teams, venue, city, category, and features.
3. Support filters such as city, sport, category, price, venue, team, and match date.
4. Keep PostgreSQL as the main source of truth.
5. Keep SQL fallback active if Elasticsearch is unavailable.

The main API affected by this work is:

```text
GET /api/tickets/search
```

Optional suggestion endpoint, if integrated later by Saghar:

```text
GET /api/search/suggestions
```

---

## 2. Files Added or Updated by Sarina

```text
elastic/ticket_index_mapping.json
elastic/sample_elastic_requests.md
backend/src/elastic/ElasticTicketSearch.java
backend/src/services/SearchService.java
backend/src/repositories/TicketRepository.java
backend/src/controllers/TicketController.java
docs/phase4-elastic.md
```

No frontend files are changed in this part.  
`Main.java` and `Router.java` are not changed here and must be handled by Saghar during final integration.

---

## 3. Elasticsearch Index

Index name:

```text
tickets
```

Mapping file:

```text
elastic/ticket_index_mapping.json
```

The index contains searchable ticket documents. Each document represents one ticket and uses `ticket_id` as the Elasticsearch document id.

Example document id:

```text
/tickets/_doc/15
```

This means the Elasticsearch document id and PostgreSQL `ticket_id` are the same.

---

## 4. Ticket Document Structure

Each Elasticsearch document contains these important fields:

```json
{
  "ticket_id": 1,
  "match_id": 1,
  "sport": "Football",
  "home_team": "Persepolis",
  "away_team": "Esteghlal",
  "match_title": "Persepolis vs Esteghlal",
  "venue": "Azadi Stadium",
  "city": "Tehran",
  "match_time": "2026-07-30T18:00:00",
  "category": "VIP",
  "section_name": "A",
  "row_number": "3",
  "seat_number": "12",
  "price": 1500000,
  "available_capacity": 1,
  "ticket_status": "AVAILABLE",
  "features": ["parking", "catering"],
  "suggest": "Football Persepolis Esteghlal Azadi Stadium Tehran VIP",
  "search_text": "Football Persepolis Esteghlal Azadi Stadium Tehran VIP parking catering",
  "indexed_at": "2026-07-27T12:00:00"
}
```

---

## 5. Searchable Fields

The main searchable fields are:

```text
search_text
match_title
sport
home_team
away_team
venue
city
category
features
```

The main filterable fields are:

```text
sport.keyword
city.keyword
venue.keyword
category.keyword
ticket_status.keyword
price
match_time
```

The `suggest` field is used for autocomplete or simple suggestion search.

---

## 6. Environment Variables

`ElasticTicketSearch.java` reads Elasticsearch settings from environment variables.

```text
ELASTIC_URL
ELASTIC_TICKET_INDEX
ELASTIC_TIMEOUT_SECONDS
```

Default values:

```text
ELASTIC_URL=http://localhost:9200
ELASTIC_TICKET_INDEX=tickets
ELASTIC_TIMEOUT_SECONDS=2
```

If no environment variable is set, the backend uses the default local Elasticsearch instance.

---

## 7. How to Create the Index

First, make sure Elasticsearch is running.

Health check:

```bash
curl -X GET "http://localhost:9200"
```

Create index with mapping:

```bash
curl -X PUT "http://localhost:9200/tickets" \
  -H "Content-Type: application/json" \
  --data-binary @elastic/ticket_index_mapping.json
```

PowerShell version:

```powershell
Invoke-RestMethod `
  -Method Put `
  -Uri "http://localhost:9200/tickets" `
  -ContentType "application/json" `
  -InFile "elastic/ticket_index_mapping.json"
```

---

## 8. Search Flow in Backend

The search flow is implemented in `SearchService`.

Flow:

```text
GET /api/tickets/search
        |
        v
TicketController.searchTickets()
        |
        v
SearchService.searchTickets()
        |
        v
Try Elasticsearch search
        |
        |-- success --> load final ticket data from PostgreSQL by ticket_id
        |
        |-- fail ----> fallback to SQL search
```

PostgreSQL remains the final source of full ticket details.

Elasticsearch is used to find matching `ticket_id` values quickly.

---

## 9. SQL Fallback

Elasticsearch is optional for local demo safety.

If Elasticsearch is down, not installed, times out, or returns an invalid response, the backend automatically continues with SQL search.

Example response when Elasticsearch is used:

```json
{
  "success": true,
  "searchEngine": "elasticsearch",
  "fallbackUsed": false,
  "filters": {
    "sport": "Football",
    "city": "Tehran"
  },
  "data": [],
  "count": 0
}
```

Example response when SQL fallback is used:

```json
{
  "success": true,
  "searchEngine": "sql",
  "fallbackUsed": true,
  "filters": {
    "sport": "Football",
    "city": "Tehran"
  },
  "data": [],
  "count": 0
}
```

This keeps the project functional even if Elasticsearch is not available on a team member's device.

---

## 10. Supported Search Query Parameters

The API supports these query parameters:

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

Example:

```text
GET /api/tickets/search?sport=Football&city=Tehran&category=VIP&min_price=500000&max_price=2000000
```

Another example:

```text
GET /api/tickets/search?team=Esteghlal&venue=Azadi&match_date=2026-07-30
```

`match_date` format:

```text
YYYY-MM-DD
```

---

## 11. Sync Strategy Between PostgreSQL and Elasticsearch

PostgreSQL is the main database and the source of truth.

Elasticsearch must be synced from PostgreSQL.

Recommended strategy:

1. When a ticket is created in PostgreSQL, index the ticket in Elasticsearch.
2. When a ticket price, status, match time, venue, or category changes, update the same Elasticsearch document.
3. When a ticket is deleted, delete the Elasticsearch document.
4. When a ticket becomes unavailable, update `ticket_status` in Elasticsearch.
5. If full re-sync is needed, delete the index, recreate it with the mapping, then index all available tickets again from PostgreSQL.

Important rule:

```text
PostgreSQL ticket_id = Elasticsearch document _id
```

This makes sync simple and prevents duplicate documents.

---

## 12. Sync Operations in ElasticTicketSearch

`ElasticTicketSearch.java` provides these operations:

```text
isAvailable()
searchTicketsRaw(filter)
searchTicketIds(filter)
suggestionsRaw(query, size)
indexTicket(ticket)
deleteTicket(ticketId)
```

`indexTicket(ticket)` is used for insert/update sync.

`deleteTicket(ticketId)` is used when a ticket should be removed from Elasticsearch.

---

## 13. Manual Reindex Plan

For demo or recovery, the team can use this manual plan:

1. Run PostgreSQL schema and seed scripts.
2. Start Elasticsearch.
3. Create index using `ticket_index_mapping.json`.
4. Read available tickets from PostgreSQL.
5. Index each ticket into Elasticsearch using `ticket_id` as `_id`.
6. Test `GET /api/tickets/search`.

If reindexing fails, the backend still works through SQL fallback.

---

## 14. Autocomplete / Suggestion

The mapping includes a suggestion field.

Example query:

```bash
curl -X GET "http://localhost:9200/tickets/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "size": 5,
    "_source": [
      "ticket_id",
      "match_title",
      "sport",
      "home_team",
      "away_team",
      "venue",
      "city",
      "category"
    ],
    "query": {
      "multi_match": {
        "query": "aza",
        "type": "bool_prefix",
        "fields": [
          "suggest",
          "suggest._2gram",
          "suggest._3gram"
        ]
      }
    }
  }'
```

This can be connected later to:

```text
GET /api/search/suggestions?q=aza
```

Route integration should be done by Saghar if the team decides to expose this endpoint.

---

## 15. Test Checklist

### Elasticsearch Test

```bash
curl -X GET "http://localhost:9200"
```

Expected: Elasticsearch returns JSON cluster information.

### Index Creation Test

```bash
curl -X PUT "http://localhost:9200/tickets" \
  -H "Content-Type: application/json" \
  --data-binary @elastic/ticket_index_mapping.json
```

Expected: Elasticsearch returns acknowledgement.

### Search API Test

```bash
curl -X GET "http://localhost:8080/api/tickets/search?city=Tehran&sport=Football"
```

Expected: JSON response with:

```text
success
searchEngine
fallbackUsed
filters
data
count
```

### Fallback Test

1. Stop Elasticsearch.
2. Run the same API request again.

```bash
curl -X GET "http://localhost:8080/api/tickets/search?city=Tehran&sport=Football"
```

Expected:

```json
{
  "searchEngine": "sql",
  "fallbackUsed": true
}
```

---

## 16. Notes for Final Integration

Sarina's part does not modify `Main.java` or `Router.java`.

If the optional suggestion endpoint is needed, Saghar can add a route later:

```java
router.get("/api/search/suggestions", ticketController::suggestTickets);
```

Only add this if the corresponding controller method is implemented during integration.

For now, the required API remains:

```text
GET /api/tickets/search
```

---

## 17. Summary

This phase adds Elasticsearch support for ticket search while preserving SQL fallback.

The implementation is safe for local demo because the system still works even without Elasticsearch.

Main benefits:

```text
Faster search
Text search over multiple fields
Filter support
Autocomplete-ready mapping
PostgreSQL and Elasticsearch sync strategy
Demo-safe SQL fallback
```
