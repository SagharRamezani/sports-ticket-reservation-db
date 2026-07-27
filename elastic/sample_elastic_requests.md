# Elasticsearch Sample Requests

Project: Sports Match Ticket Reservation System  
Phase: 4 - Elasticsearch Search Backend  
Owner: Sarina

This file contains sample Elasticsearch requests for creating the ticket index, inserting sample ticket documents, testing search filters, and testing autocomplete suggestions.

> Default Elasticsearch URL used in examples:
>
> `http://localhost:9200`

---

## 1. Check Elasticsearch Health

```bash
curl -X GET "http://localhost:9200"
```

Expected result: Elasticsearch should return cluster information in JSON format.

---

## 2. Create Ticket Index

Before running this request, make sure `elastic/ticket_index_mapping.json` exists.

### PowerShell

```powershell
Invoke-RestMethod `
  -Method Put `
  -Uri "http://localhost:9200/tickets" `
  -ContentType "application/json" `
  -InFile "elastic/ticket_index_mapping.json"
```

### Git Bash / Linux / macOS

```bash
curl -X PUT "http://localhost:9200/tickets" \
  -H "Content-Type: application/json" \
  --data-binary @elastic/ticket_index_mapping.json
```

---

## 3. Delete Ticket Index

Use this only when you want to recreate the mapping.

```bash
curl -X DELETE "http://localhost:9200/tickets"
```

---

## 4. Insert One Sample Ticket Document

```bash
curl -X PUT "http://localhost:9200/tickets/_doc/1" \
  -H "Content-Type: application/json" \
  -d '{
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
    "features": ["parking", "catering", "covered stand"],
    "search_text": "Football Persepolis Esteghlal Azadi Stadium Tehran VIP parking catering covered stand",
    "indexed_at": "2026-07-27T12:00:00"
  }'
```

---

## 5. Insert Multiple Sample Ticket Documents

```bash
curl -X POST "http://localhost:9200/tickets/_bulk" \
  -H "Content-Type: application/x-ndjson" \
  --data-binary '
{"index":{"_id":"2"}}
{"ticket_id":2,"match_id":2,"sport":"Volleyball","home_team":"Paykan","away_team":"Saipa","match_title":"Paykan vs Saipa","venue":"Azadi Volleyball Hall","city":"Tehran","match_time":"2026-08-01T16:00:00","category":"NORMAL","section_name":"B","row_number":"5","seat_number":"20","price":500000,"available_capacity":1,"ticket_status":"AVAILABLE","features":["near court"],"search_text":"Volleyball Paykan Saipa Azadi Volleyball Hall Tehran NORMAL near court","indexed_at":"2026-07-27T12:00:00"}
{"index":{"_id":"3"}}
{"ticket_id":3,"match_id":3,"sport":"Basketball","home_team":"Mahram","away_team":"Zob Ahan","match_title":"Mahram vs Zob Ahan","venue":"Enghelab Hall","city":"Tehran","match_time":"2026-08-04T19:30:00","category":"SPECIAL","section_name":"C","row_number":"2","seat_number":"7","price":800000,"available_capacity":1,"ticket_status":"AVAILABLE","features":["vip lounge","dedicated entrance"],"search_text":"Basketball Mahram Zob Ahan Enghelab Hall Tehran SPECIAL vip lounge dedicated entrance","indexed_at":"2026-07-27T12:00:00"}
'
```

---

## 6. Refresh Index After Inserts

```bash
curl -X POST "http://localhost:9200/tickets/_refresh"
```

---

## 7. Search by General Text

Search in `search_text`, `match_title`, teams, venue, city, sport, and category.

```bash
curl -X GET "http://localhost:9200/tickets/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "multi_match": {
        "query": "azadi football vip",
        "fields": [
          "search_text",
          "match_title",
          "sport",
          "home_team",
          "away_team",
          "venue",
          "city",
          "category",
          "features"
        ]
      }
    }
  }'
```

---

## 8. Filter by Sport

```bash
curl -X GET "http://localhost:9200/tickets/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "bool": {
        "filter": [
          { "term": { "sport.keyword": "Football" } },
          { "term": { "ticket_status.keyword": "AVAILABLE" } }
        ]
      }
    }
  }'
```

---

## 9. Filter by City and Category

```bash
curl -X GET "http://localhost:9200/tickets/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "bool": {
        "filter": [
          { "term": { "city.keyword": "Tehran" } },
          { "term": { "category.keyword": "VIP" } },
          { "term": { "ticket_status.keyword": "AVAILABLE" } }
        ]
      }
    },
    "sort": [
      { "match_time": { "order": "asc" } }
    ]
  }'
```

---

## 10. Filter by Price Range

```bash
curl -X GET "http://localhost:9200/tickets/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "bool": {
        "filter": [
          {
            "range": {
              "price": {
                "gte": 300000,
                "lte": 1000000
              }
            }
          },
          { "term": { "ticket_status.keyword": "AVAILABLE" } }
        ]
      }
    },
    "sort": [
      { "price": { "order": "asc" } }
    ]
  }'
```

---

## 11. Filter by Match Date

```bash
curl -X GET "http://localhost:9200/tickets/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "bool": {
        "filter": [
          {
            "range": {
              "match_time": {
                "gte": "2026-08-01T00:00:00",
                "lt": "2026-08-02T00:00:00"
              }
            }
          },
          { "term": { "ticket_status.keyword": "AVAILABLE" } }
        ]
      }
    }
  }'
```

---

## 12. Combined Search and Filters

This is the closest example to `GET /api/tickets/search`.

```bash
curl -X GET "http://localhost:9200/tickets/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "bool": {
        "must": [
          {
            "multi_match": {
              "query": "azadi",
              "fields": [
                "search_text",
                "match_title",
                "home_team",
                "away_team",
                "venue",
                "city"
              ]
            }
          }
        ],
        "filter": [
          { "term": { "sport.keyword": "Football" } },
          { "term": { "city.keyword": "Tehran" } },
          {
            "range": {
              "price": {
                "gte": 500000,
                "lte": 2000000
              }
            }
          },
          { "term": { "ticket_status.keyword": "AVAILABLE" } }
        ]
      }
    },
    "sort": [
      { "match_time": { "order": "asc" } },
      { "price": { "order": "asc" } }
    ]
  }'
```

---

## 13. Autocomplete / Suggestion Search

This request uses the `search_as_you_type` or autocomplete analyzer fields from the mapping.

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

---

## 14. Update a Ticket Document

Use this when ticket data changes in PostgreSQL and should be synced to Elasticsearch.

```bash
curl -X POST "http://localhost:9200/tickets/_update/1" \
  -H "Content-Type: application/json" \
  -d '{
    "doc": {
      "price": 1400000,
      "ticket_status": "AVAILABLE",
      "indexed_at": "2026-07-27T13:00:00"
    }
  }'
```

---

## 15. Delete a Ticket Document

Use this when a ticket is deleted or should no longer appear in search.

```bash
curl -X DELETE "http://localhost:9200/tickets/_doc/1"
```

---

## 16. SQL to Elasticsearch Sync Strategy

The backend should use PostgreSQL as the main source of truth.

Recommended sync flow:

1. Read searchable ticket data from PostgreSQL using `TicketRepository`.
2. Convert each ticket row into an Elasticsearch ticket document.
3. Use `ticket_id` as the Elasticsearch document `_id`.
4. If a ticket is inserted or updated in PostgreSQL, re-index the same `ticket_id`.
5. If a ticket is deleted or cancelled and should not appear in search, delete or update the Elasticsearch document.
6. If Elasticsearch is not available, keep using SQL search fallback.

---

## 17. Backend Fallback Rule

`GET /api/tickets/search` should try Elasticsearch first.

If Elasticsearch is unavailable, times out, or returns an invalid response, the backend should continue with SQL search.

Expected behavior:

```text
ElasticSearch available    -> use ElasticTicketSearch
ElasticSearch unavailable  -> use TicketRepository.searchTickets
```

This keeps the project demo-safe even if Elasticsearch is not installed on every team member's system.
