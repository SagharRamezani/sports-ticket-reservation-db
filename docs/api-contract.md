# API Contract

Base URL:

http://localhost:8080

## Auth

### Signup

POST /api/auth/signup

Request:

{
"first_name": "Ali",
"last_name": "Ahmadi",
"email": "ali@example.com",
"phone": "09120000000",
"password": "123456"
}

Response:

{
"success": true,
"message": "User registered successfully",
"token": "jwt-token"
}

## Ticket Search

GET /api/tickets/search?sport=football&city=Tehran

Response:

{
"success": true,
"data": [
{
"ticket_id": 1,
"sport": "football",
"home_team": "Persepolis",
"away_team": "Esteghlal",
"venue": "Azadi Stadium",
"city": "Tehran",
"match_time": "2026-02-20T18:00:00",
"category": "VIP",
"price": 1500000,
"available_count": 25
}
]
}
