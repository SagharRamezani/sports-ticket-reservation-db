# Phase 2 Report

## Overview

Phase 2 focuses on creating and testing the final relational database structure for the Sports Match Ticket Reservation System.

The project uses PostgreSQL as the relational database. In this phase, the team finalized the database tables, constraints, indexes, seed data, analytical queries, and stored procedures required for testing the core logic of the system.

The main goal of this phase is to make sure the database can support:

- user registration and role management
- ticket search and filtering
- sport match management
- ticket reservation
- successful, failed, pending, cancelled, and refunded payments
- cancellation and refund workflow
- user reports and support actions
- analytical queries for project evaluation

## Team Responsibility in Phase 2

### Saghar

Saghar is responsible for:

- final PostgreSQL schema
- table definitions
- primary keys
- foreign keys
- unique constraints
- check constraints
- indexes
- analytical queries 1 to 7
- base `run_all.sql` coordination

Main files:

```text
database/schema/01_tables.sql
database/schema/02_constraints.sql
database/schema/03_indexes.sql
database/queries/q01_to_q07.sql
database/run_all.sql
```

### Shamim

Shamim is responsible for:

- seed data
- Phase 2 report
- screenshot checklist
- documentation updates related to seed and testing

Main files:

```text
database/seed/01_users_seed.sql
database/seed/02_sports_venues_teams_seed.sql
database/seed/03_matches_tickets_seed.sql
database/seed/04_reservations_payments_seed.sql
database/seed/05_reports_refunds_support_seed.sql
docs/phase2-report.md
```

### Sarina

Sarina is responsible for:

- analytical queries 8 to 22
- stored procedures 1 to 8
- logical testing of queries and procedures

Expected files:

```text
database/queries/q08_to_q15.sql
database/queries/q16_to_q22.sql
database/procedures/p01_to_p04.sql
database/procedures/p05_to_p08.sql
```

## Final Tables

The database contains the following final tables.

### Core Reference Tables

| Table | Purpose |
|---|---|
| `roles` | Stores user roles such as customer and support. |
| `cities` | Stores city and province information. |
| `sports` | Stores sport types such as football, volleyball, and basketball. |
| `payment_methods` | Stores available payment methods. |
| `ticket_categories` | Stores ticket category codes such as NORMAL, VIP, and SPECIAL. |
| `report_categories` | Stores categories for user reports. |
| `features` | Stores optional ticket features such as parking or catering. |

### User and Venue Tables

| Table | Purpose |
|---|---|
| `users` | Stores customer and support user information. |
| `venues` | Stores stadiums, halls, and arenas. |
| `teams` | Stores teams for each sport and city. |

### Match and Ticket Tables

| Table | Purpose |
|---|---|
| `matches` | Stores sport match information. |
| `tickets` | Stores ticket price, status, seat, row, section, and category. |
| `ticket_features` | Many-to-many bridge table between tickets and features. |
| `football_details` | Stores football-specific ticket details. |
| `volleyball_details` | Stores volleyball-specific ticket details. |
| `basketball_details` | Stores basketball-specific ticket details. |

### Reservation, Payment, and Support Tables

| Table | Purpose |
|---|---|
| `reservations` | Stores reservation status, reservation time, expiration time, and cancellation data. |
| `payments` | Stores payment transaction data. |
| `cancellation_policies` | Stores refund penalty rules. |
| `refunds` | Stores refund requests and refund processing status. |
| `reports` | Stores user reports about tickets, reservations, or payments. |
| `support_actions` | Stores support actions for reports or reservations. |
| `otp_logs` | Stores OTP logs for login, signup, password reset, and profile update workflows. |

## Design Notes

The final database structure follows the ERD and normalization output from Phase 1.

Important design points:

- A user belongs to one role.
- A user may belong to one city.
- A venue belongs to one city.
- A team belongs to one sport and one city.
- A match belongs to one sport and one venue.
- Each match has a home team and an away team.
- A ticket belongs to one match and one ticket category.
- A reservation belongs to one user and one ticket.
- A payment belongs to one reservation, one user, and one payment method.
- A report belongs to one reporting user and may reference a reservation, ticket, or both.
- A support action is performed by a support user.
- Sport-specific ticket details are separated into football, volleyball, and basketball detail tables.
- Ticket features are stored in a separate table and linked through `ticket_features`.

## Constraints

The schema includes the following types of constraints:

- Primary Key constraints
- Foreign Key constraints
- Unique constraints
- Check constraints

Important examples:

- each user must have either email or phone number
- ticket price cannot be negative
- reservation expiration time must be after reservation time
- match home team and away team must be different
- reservation status is restricted to valid values
- payment status is restricted to valid values
- refund status is restricted to valid values
- report status is restricted to valid values
- OTP purpose and OTP status are restricted to valid values

## Seed Data

Seed data is split into five files to keep the insertion order clear and compatible with foreign key dependencies.

### Seed File 1

```text
database/seed/01_users_seed.sql
```

This file inserts:

- roles
- cities
- users

Coverage:

- `CUSTOMER` role
- `SUPPORT` role
- 10 cities
- 12 users
- at least 2 support users
- at least 1 old registered user
- at least 1 user intentionally left without reservation for Query 1

### Seed File 2

```text
database/seed/02_sports_venues_teams_seed.sql
```

This file inserts:

- sports
- venues
- teams

Coverage:

- football
- volleyball
- basketball
- additional sports for diversity
- Azadi Stadium
- venues in Tehran and other cities
- football, volleyball, and basketball teams

### Seed File 3

```text
database/seed/03_matches_tickets_seed.sql
```

This file inserts:

- ticket categories
- matches
- tickets
- features
- ticket features
- football details
- volleyball details
- basketball details
- cancellation policies

Coverage:

- football, volleyball, and basketball matches
- VIP, NORMAL, SPECIAL, ECONOMY, and FAMILY ticket categories
- different ticket prices
- ticket statuses such as AVAILABLE, RESERVED, SOLD, CANCELLED, and UNAVAILABLE
- ticket features such as parking, catering, covered stand, and VIP lounge
- cancellation policies with different penalty percentages

### Seed File 4

```text
database/seed/04_reservations_payments_seed.sql
```

This file inserts:

- payment methods
- reservations
- payments

Coverage:

- paid reservations
- pending reservations
- cancelled reservations
- expired reservations
- successful payments
- failed payments
- pending payments
- cancelled payments
- refunded payments
- purchases today
- purchases yesterday
- purchases in the recent week
- successful purchases in football, volleyball, and basketball
- purchases in Tehran province

Important note:

The schema does not use `RESERVED` as a value for `reservation_status`. Temporary reservations are represented using:

```text
PENDING
```

### Seed File 5

```text
database/seed/05_reports_refunds_support_seed.sql
```

This file inserts:

- refunds
- report categories
- reports
- support actions
- OTP logs

Coverage:

- refund statuses: PENDING, APPROVED, REJECTED, PROCESSED
- report categories: payment, seat, price, cancellation, schedule, and venue issues
- multiple reports for the same ticket to support Query 22
- support actions by support users
- OTP statuses: PENDING, VERIFIED, EXPIRED, FAILED

## Analytical Queries

The analytical queries are used to validate that the database design and seed data are meaningful.

### Queries 1 to 7

These queries are handled by Saghar.

| Query | Purpose |
|---|---|
| 1 | Find users who have never reserved any ticket. |
| 2 | Find users who bought at least one ticket. |
| 3 | Show monthly total successful payments by user. |
| 4 | Show users who bought exactly once in each venue city. |
| 5 | Find user information for the newest purchased ticket. |
| 6 | Find users whose total payments are greater than the average payment total. |
| 7 | Count sold tickets by sport type. |

### Queries 8 to 22

These queries are handled by Sarina.

| Query | Purpose |
|---|---|
| 8 | Find top 3 users with the most ticket purchases in the recent week. |
| 9 | Count sold tickets in Tehran province by city. |
| 10 | List cities where the oldest registered user has purchased tickets. |
| 11 | List support users. |
| 12 | List users who bought at least two tickets. |
| 13 | List users who bought at most two tickets from a specific sport type. |
| 14 | List users who bought at least one ticket from all main sport types. |
| 15 | List tickets purchased today ordered by purchase time. |
| 16 | Find the second best-selling ticket. |
| 17 | Find the support user with the most cancelled reservations and cancellation percentage. |
| 18 | Update the last name of the user with the most cancelled tickets to Redington. |
| 19 | Delete all cancelled tickets for user Redington. |
| 20 | Delete all cancelled tickets in the system. |
| 21 | Reduce yesterday's sold Azadi Stadium ticket prices by 10 percent. |
| 22 | Show report subject and count for the most reported ticket. |

## Stored Procedures

Stored procedures are planned for repeated and analytical operations.

| Procedure | Purpose |
|---|---|
| 1 | Get purchased tickets by user email or phone. |
| 2 | Get users whose reservations were cancelled by a support user. |
| 3 | Get purchased tickets by city name. |
| 4 | Search tickets by a text phrase in spectator name, teams, venue, or category. |
| 5 | Get users from the same city as a given user. |
| 6 | Get top N buyers after a selected date. |
| 7 | Get cancelled tickets by sport type ordered by date. |
| 8 | Get users with the most reports in a selected report category. |

## Indexing and Optimization

Indexes are used to improve common searches, filters, and joins.

Main indexed areas:

- users by role, city, status, and registration date
- cities by province
- venues by city and type
- teams by sport and city
- matches by sport, venue, status, and start time
- tickets by match, category, status, and price
- reservations by user, ticket, status, and expiration time
- payments by user, reservation, status, paid time, and method
- reports by user, ticket, reservation, category, and status
- refunds by reservation, payment, status, and request time
- OTP logs by user, status, and expiration time
- sport-specific details by league, stadium, or hall

Partial indexes are useful for frequent cases such as:

- pending reservations by expiration time
- successful payments by paid time
- pending OTP logs by expiration time

## Test Plan

The database should be tested from a clean PostgreSQL database.

Suggested test order:

```text
1. Create a clean database.
2. Run schema files.
3. Run constraints file.
4. Run indexes file.
5. Run seed files in order.
6. Run analytical queries.
7. Run stored procedures.
8. Capture screenshots.
```

Suggested command:

```bash
psql -U postgres -d sports_ticket_db -f database/run_all.sql
```

If seed files are not yet included in `run_all.sql`, they can be run manually in this order:

```bash
psql -U postgres -d sports_ticket_db -f database/seed/01_users_seed.sql
psql -U postgres -d sports_ticket_db -f database/seed/02_sports_venues_teams_seed.sql
psql -U postgres -d sports_ticket_db -f database/seed/03_matches_tickets_seed.sql
psql -U postgres -d sports_ticket_db -f database/seed/04_reservations_payments_seed.sql
psql -U postgres -d sports_ticket_db -f database/seed/05_reports_refunds_support_seed.sql
```

## Test Screenshots

Screenshots should be saved under:

```text
docs/screenshots/phase2/
```

Recommended screenshot checklist:

| Screenshot | Description |
|---|---|
| `01_database_created.png` | Clean database creation. |
| `02_schema_tables_success.png` | Successful execution of table creation file. |
| `03_constraints_success.png` | Successful execution of constraints file. |
| `04_indexes_success.png` | Successful execution of indexes file. |
| `05_seed_users_success.png` | Successful execution of users seed file. |
| `06_seed_sports_venues_teams_success.png` | Successful execution of sports, venues, teams seed file. |
| `07_seed_matches_tickets_success.png` | Successful execution of matches and tickets seed file. |
| `08_seed_reservations_payments_success.png` | Successful execution of reservations and payments seed file. |
| `09_seed_reports_refunds_support_success.png` | Successful execution of reports, refunds, support seed file. |
| `10_sample_users.png` | Sample output from `users`. |
| `11_sample_matches_tickets.png` | Sample output from `matches` and `tickets`. |
| `12_sample_reservations_payments.png` | Sample output from `reservations` and `payments`. |
| `13_sample_reports_refunds.png` | Sample output from `reports` and `refunds`. |
| `14_queries_success.png` | Successful execution of analytical queries. |
| `15_procedures_success.png` | Successful execution of stored procedures. |
| `16_final_run_all_success.png` | Final successful execution of `run_all.sql`. |

## Suggested Verification Queries

These simple queries can be used to verify inserted seed data.

```sql
SELECT COUNT(*) AS users_count FROM users;
SELECT COUNT(*) AS support_users FROM users u JOIN roles r ON u.role_id = r.role_id WHERE r.role_code = 'SUPPORT';
SELECT COUNT(*) AS sports_count FROM sports;
SELECT COUNT(*) AS venues_count FROM venues;
SELECT COUNT(*) AS teams_count FROM teams;
SELECT COUNT(*) AS matches_count FROM matches;
SELECT COUNT(*) AS tickets_count FROM tickets;
SELECT COUNT(*) AS reservations_count FROM reservations;
SELECT COUNT(*) AS payments_count FROM payments;
SELECT COUNT(*) AS reports_count FROM reports;
SELECT COUNT(*) AS refunds_count FROM refunds;
```

Additional verification:

```sql
SELECT ticket_status, COUNT(*) FROM tickets GROUP BY ticket_status;
SELECT reservation_status, COUNT(*) FROM reservations GROUP BY reservation_status;
SELECT payment_status, COUNT(*) FROM payments GROUP BY payment_status;
SELECT refund_status, COUNT(*) FROM refunds GROUP BY refund_status;
SELECT report_status, COUNT(*) FROM reports GROUP BY report_status;
```

## Notes and Issues

- PostgreSQL is the selected relational database.
- Seed files use explicit IDs to keep references stable across later seed files, queries, and procedures.
- `OVERRIDING SYSTEM VALUE` is used because the schema uses identity columns.
- The seed files must be executed in the correct order because of foreign key dependencies.
- Temporary reservations are represented by `PENDING` because the schema does not allow `RESERVED` in `reservation_status`.
- Backend, frontend, Redis, and ElasticSearch implementation are outside the direct scope of this Phase 2 database report.
- If a query or stored procedure needs a schema change, it should be reported to Saghar instead of directly changing schema files.
- If a query returns an empty or weak result, the seed data should be adjusted by Shamim.
- If final integration requires running seed files from `run_all.sql`, this should be coordinated with Saghar.
