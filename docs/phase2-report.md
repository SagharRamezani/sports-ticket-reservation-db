# Phase 2 Report

## Overview

This report documents Phase 2 of the Sports Match Ticket Reservation System database project.

In this phase, the team finalized the relational database structure, added seed data, implemented analytical SQL queries, prepared stored procedures, and tested the database scripts using PostgreSQL.

## Final Tables

The final database structure includes the main entities required for the reservation and ticket purchasing workflow:

- roles
- cities
- sports
- users
- venues
- teams
- matches
- ticket_categories
- tickets
- reservations
- payment_methods
- payments
- cancellation_policies
- refunds
- report_categories
- reports
- support_actions
- otp_logs
- features
- ticket_features
- football_details
- volleyball_details
- basketball_details

The tables are designed based on the ERD and normalized structure from Phase 1. Primary keys, foreign keys, unique constraints, check constraints, and indexes are handled in the schema files.

## Seed Data

Seed data is split into separate files to keep the insertion order clear and compatible with foreign key constraints:

1. `database/seed/01_users_seed.sql`
    - roles
    - cities
    - users

2. `database/seed/02_sports_venues_teams_seed.sql`
    - sports
    - venues
    - teams

3. `database/seed/03_matches_tickets_seed.sql`
    - ticket categories
    - matches
    - tickets
    - features
    - ticket features
    - football details
    - volleyball details
    - basketball details
    - cancellation policies

4. `database/seed/04_reservations_payments_seed.sql`
    - payment methods
    - reservations
    - payments

5. `database/seed/05_reports_refunds_support_seed.sql`
    - refunds
    - report categories
    - reports
    - support actions
    - OTP logs

The seed data covers normal users, support users, cities, venues, sports, teams, matches, tickets, successful purchases, pending reservations, cancelled reservations, expired reservations, failed payments, refunded payments, reports, support actions, and OTP records.

## Analytical Queries

The project includes analytical SQL queries for testing and validating the database design.

Query coverage includes:

- users without reservations
- users with successful purchases
- monthly payment totals
- purchases by city
- latest purchased ticket
- users with payments above average
- sold tickets by sport type
- top buyers in the recent week
- sold tickets in Tehran province
- oldest registered user purchase cities
- support users
- users with at least two purchases
- sport-specific purchase limits
- users who bought tickets for all main sports
- tickets purchased today
- second best-selling ticket
- support cancellation statistics
- update and delete operations for cancelled tickets
- report statistics for the most reported ticket

## Stored Procedures

Stored procedures are planned for repeated and analytical operations such as:

- listing purchased tickets by email or phone
- listing users whose reservations were cancelled by a support user
- listing purchased tickets by city
- searching tickets by a text phrase
- finding users from the same city
- listing top buyers after a selected date
- listing cancelled tickets by sport type
- listing users with the most reports in a report category

## Indexing and Optimization

Indexes are included to improve frequent search and filtering operations.

The main indexed areas are:

- users by role, city, status, and registration date
- matches by sport, venue, status, and start time
- tickets by match, category, status, and price
- reservations by user, ticket, status, and expiration time
- payments by user, status, payment date, and method
- reports by category, status, user, ticket, and reservation
- refunds by status and request date
- OTP logs by user, status, and expiration time

## Test Screenshots

Screenshots should be collected from PostgreSQL or the terminal after running the project SQL scripts.

Required screenshots checklist:

- clean database creation
- successful execution of schema files
- successful execution of constraints file
- successful execution of indexes file
- successful execution of seed files
- sample output from users table
- sample output from matches and tickets tables
- sample output from reservations and payments tables
- sample output from reports and refunds tables
- successful execution of analytical queries
- successful execution of stored procedures
- final successful run of `database/run_all.sql`

Suggested screenshot folder:

```text
docs/screenshots/phase2/
```

## Notes and Issues

- PostgreSQL is used as the relational database.
- Seed data uses explicit IDs so later seed files, queries, reports, and procedures can reference stable records.
- `OVERRIDING SYSTEM VALUE` is used because schema tables use identity columns.
- The schema uses `PENDING` for temporary reservations instead of `RESERVED`.
- The seed files are ordered according to foreign key dependencies.
- Backend, frontend, Redis, and ElasticSearch implementation are outside the scope of this Phase 2 database report.
