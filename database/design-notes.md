# Database Design Notes

## Project

Sports Match Ticket Reservation System

This document explains the phase 1 database design decisions for the ticket reservation and purchase system. The current
phase focuses only on ERD preparation, initial schema design, constraints, normalization, and indexing.

The selected database is PostgreSQL. Later phases will use raw SQL through JDBC without ORM.

## Phase 1 Scope

Phase 1 includes:

- Initial table design
- Primary key and foreign key planning
- Unique and check constraints
- 3NF-oriented schema structure
- Initial indexes for search and reservation workflows
- Preparation for ERD and later backend implementation

Phase 1 does not include:

- Seed data
- Backend implementation
- Java code
- JDBC integration
- API design
- Frontend implementation
- Stored procedures

## Technology Decisions

| Layer           | Technology                    |
|-----------------|-------------------------------|
| Backend         | Raw Java                      |
| Database        | PostgreSQL                    |
| Database Access | JDBC with raw SQL             |
| Frontend        | HTML, CSS, Vanilla JavaScript |

## File Ownership

Saghar owns the main database schema files:

- `database/schema/01_tables.sql`
- `database/schema/02_constraints.sql`
- `database/schema/03_indexes.sql`
- `database/design-notes.md`

Other team members should not directly edit these files during phase 1. If a schema issue is found, it should be
reported to Saghar through review notes or team communication.

## Main Design Strategy

The schema is designed around the following principles:

1. Keep the design simple and suitable for phase 1.
2. Keep the schema normalized and close to 3NF.
3. Separate table definitions, constraints, and indexes into different files.
4. Use lookup tables for entities that may grow or need display names.
5. Use check constraints for limited status values.
6. Avoid backend, seed data, or API-specific logic in phase 1.

## SQL File Separation

The schema is split into three SQL files:

### `01_tables.sql`

Contains only initial table definitions, columns, data types, default values, and `NOT NULL` rules.

### `02_constraints.sql`

Contains:

- Primary keys
- Foreign keys
- Unique constraints
- Check constraints

### `03_indexes.sql`

Contains indexes for search, reservation, payment, report, support, and OTP workflows.

This separation makes the schema easier to review and reduces merge conflicts.

## Lookup Tables vs Check Constraints

The design uses lookup tables for values that are expected to grow, need metadata, or may be shown in the UI.

Lookup tables include:

- `roles`
- `cities`
- `sports`
- `ticket_categories`
- `payment_methods`
- `report_categories`
- `features`

Status fields are stored as text columns with `CHECK` constraints instead of separate status lookup tables.

Status fields include:

- `users.account_status`
- `matches.match_status`
- `tickets.ticket_status`
- `reservations.reservation_status`
- `payments.payment_status`
- `refunds.refund_status`
- `reports.report_status`
- `otp_logs.otp_status`

This keeps the phase 1 design simpler while still preventing invalid status values.

## Table Groups

## 1. Core Identity and Location Tables

### `roles`

Stores system roles such as normal user and support user.

Important columns:

- `role_id`
- `role_code`
- `role_name`

Reasoning:

Roles are stored in a separate table instead of hardcoded values because user permissions may grow in later phases.

### `cities`

Stores city and province information.

Important columns:

- `city_id`
- `city_name`
- `province_name`

Reasoning:

Cities are separated to avoid repeating city and province names in users, venues, and teams.

### `users`

Stores registered users and support users.

Important columns:

- `user_id`
- `role_id`
- `city_id`
- `first_name`
- `last_name`
- `email`
- `phone_number`
- `password_hash`
- `account_status`

Reasoning:

A single users table is used for both spectators and support users. The role is determined using `role_id`. This avoids
duplicate user tables and keeps authentication data centralized.

A check constraint requires at least one contact method: email or phone number.

## 2. Sport, Venue, Team, and Match Tables

### `sports`

Stores sport types such as football, volleyball, and basketball.

Reasoning:

Sports are stored separately so matches and teams can reference a normalized sport entity.

### `venues`

Stores stadiums, halls, and other match locations.

Important columns:

- `venue_id`
- `city_id`
- `venue_name`
- `address`
- `total_capacity`
- `venue_type`

Reasoning:

Venues are separated from matches because many matches can happen in the same venue.

### `teams`

Stores teams for different sports.

Important columns:

- `team_id`
- `sport_id`
- `city_id`
- `team_name`

Reasoning:

Teams belong to a sport and may also belong to a city. This avoids repeating team names in the matches table.

### `matches`

Stores sport events and match schedule information.

Important columns:

- `match_id`
- `sport_id`
- `venue_id`
- `home_team_id`
- `away_team_id`
- `match_title`
- `tournament_name`
- `match_start_time`
- `match_status`

Reasoning:

The match table connects sports, teams, and venues. Team fields are nullable so the design can also support events where
teams are not known or not applicable.

A check constraint prevents the home team and away team from being the same.

## 3. Ticket and Reservation Tables

### `ticket_categories`

Stores ticket categories such as normal, special, VIP, or other category names.

Reasoning:

Ticket categories are separated because category names and descriptions may be reused across many tickets.

### `tickets`

Stores individual reservable tickets.

Important columns:

- `ticket_id`
- `match_id`
- `ticket_category_id`
- `section_name`
- `row_number`
- `seat_number`
- `price`
- `ticket_status`

Reasoning:

Each ticket belongs to one match and one ticket category. Seat-related fields are stored on the ticket because each
ticket can represent a specific section, row, and seat.

Price is stored on the ticket so different seats and categories can have different prices.

A check constraint prevents negative prices.

### `reservations`

Stores user ticket reservations.

Important columns:

- `reservation_id`
- `user_id`
- `ticket_id`
- `reservation_status`
- `reserved_at`
- `expires_at`
- `confirmed_at`
- `cancelled_at`

Reasoning:

Reservations connect users and tickets. The `expires_at` field supports temporary reservation behavior. If payment is
not completed before expiration, the reservation can later be marked as expired.

A check constraint ensures `expires_at` is after `reserved_at`.

## 4. Payment, Cancellation, and Refund Tables

### `payment_methods`

Stores available payment methods.

Reasoning:

Payment methods are separated because they are configurable and may change later.

### `payments`

Stores payment attempts and transaction data.

Important columns:

- `payment_id`
- `reservation_id`
- `user_id`
- `payment_method_id`
- `amount`
- `payment_status`
- `transaction_reference`

Reasoning:

Payments are stored separately from reservations because one reservation may need payment tracking, transaction status,
and audit information.

The `transaction_reference` column is unique to avoid duplicate external or local transaction records.

### `cancellation_policies`

Stores cancellation penalty rules.

Important columns:

- `cancellation_policy_id`
- `match_id`
- `ticket_category_id`
- `hours_before_match`
- `penalty_percent`

Reasoning:

Cancellation rules may depend on the match and ticket category. This table allows the system to calculate penalties
based on remaining time before the match.

### `refunds`

Stores refund requests and processing results.

Important columns:

- `refund_id`
- `reservation_id`
- `payment_id`
- `amount`
- `penalty_amount`
- `refund_status`

Reasoning:

Refunds are separated from payments because refund processing has its own status, requested time, processed time, and
penalty amount.

## 5. Report and Support Tables

### `report_categories`

Stores report categories such as payment issue, seat issue, cancellation issue, or match time change.

Reasoning:

Report categories are lookup values that may be shown in the UI and reused by many reports.

### `reports`

Stores user-submitted reports.

Important columns:

- `report_id`
- `reporter_user_id`
- `reservation_id`
- `ticket_id`
- `report_category_id`
- `report_title`
- `report_text`
- `report_status`

Reasoning:

Reports can be related to either a reservation or a ticket. A check constraint requires at least one related object.

### `support_actions`

Stores support staff actions.

Important columns:

- `support_action_id`
- `report_id`
- `reservation_id`
- `support_user_id`
- `action_type`
- `action_note`

Reasoning:

Support actions are separated from reports because a report or reservation can have multiple support actions over time.

The `support_user_id` references `users`, and support access is determined by the user's role.

## 6. OTP Log Table

### `otp_logs`

Stores OTP audit records.

Important columns:

- `otp_log_id`
- `user_id`
- `email`
- `phone_number`
- `otp_code_hash`
- `otp_purpose`
- `otp_status`
- `expires_at`
- `verified_at`

Reasoning:

In later phases, active OTP values may be stored in Redis with TTL. However, this table keeps hashed OTP logs for audit
and debugging purposes.

A check constraint requires at least one related identity value: user, email, or phone number.

## 7. Feature and Sport-Specific Detail Tables

### `features`

Stores reusable ticket features such as parking, VIP service, covered stand, dedicated entrance, or catering.

Reasoning:

Features are separated into a lookup table because the list may grow and each feature can be reused across many tickets.

### `ticket_features`

Stores the many-to-many relationship between tickets and features.

Reasoning:

A ticket can have multiple features, and one feature can belong to many tickets. This relationship is normalized using a
junction table.

### `football_details`

Stores football-specific ticket details.

### `volleyball_details`

Stores volleyball-specific ticket details.

### `basketball_details`

Stores basketball-specific ticket details.

Reasoning:

Sport-specific details are separated from the main `tickets` table to avoid many nullable columns and to keep the schema
closer to 3NF.

Each sport-specific detail table has a unique constraint on `ticket_id`, so each ticket can have at most one detail row
for that sport.

## Normalization Notes

The schema is designed to be close to third normal form.

### First Normal Form

Each column stores atomic values. Repeating groups such as ticket features are moved into separate tables.

Example:

Instead of storing features as comma-separated text in `tickets`, the design uses:

- `features`
- `ticket_features`

### Second Normal Form

Many-to-many relationships are separated into junction tables.

Example:

`ticket_features` uses a composite primary key:

- `ticket_id`
- `feature_id`

This prevents duplicate feature assignments for the same ticket.

### Third Normal Form

Non-key attributes depend on the key, not on other non-key attributes.

Examples:

- City and province data are stored in `cities`, not repeated in users or venues.
- Sport names are stored in `sports`, not repeated in matches.
- Payment method names are stored in `payment_methods`, not repeated in payments.
- Report category names are stored in `report_categories`, not repeated in reports.

## Main Relationships for ERD

Important ERD relationships:

- One role can belong to many users.
- One city can have many users.
- One city can have many venues.
- One city can have many teams.
- One sport can have many teams.
- One sport can have many matches.
- One venue can host many matches.
- One match can have many tickets.
- One ticket category can belong to many tickets.
- One user can have many reservations.
- One ticket can have many reservation records over time.
- One reservation can have many payments.
- One payment method can be used by many payments.
- One reservation can have a refund.
- One payment can be related to a refund.
- One report category can belong to many reports.
- One user can submit many reports.
- One support user can perform many support actions.
- One ticket can have many features through `ticket_features`.
- One feature can belong to many tickets through `ticket_features`.
- One ticket can have at most one football detail row.
- One ticket can have at most one volleyball detail row.
- One ticket can have at most one basketball detail row.

## Constraint Strategy

Primary keys are defined for all main tables.

Foreign keys preserve relationships between entities.

Unique constraints are used for natural identifiers such as:

- Role code
- Sport code
- Payment method code
- Report category code
- Feature code
- User email
- User phone number
- Payment transaction reference

Check constraints are used for:

- Status values
- Non-negative prices and amounts
- Valid penalty percentages
- Valid reservation expiration time
- Required contact information
- Required report relationship

## Index Strategy

Initial indexes are added for common phase 2 and phase 3 query patterns.

Important indexed workflows:

- Searching matches by sport and start time
- Searching matches by venue and start time
- Finding available tickets for a match
- Filtering tickets by category and price
- Listing reservations by user
- Finding pending expired reservations
- Listing payments by user and status
- Listing reports by category and status
- Finding active OTP logs

PostgreSQL automatically creates indexes for primary key and unique constraints, so duplicate indexes were not added for
those columns.

## PostgreSQL-Specific Notes

The schema uses PostgreSQL-compatible syntax:

- `BIGINT GENERATED ALWAYS AS IDENTITY`
- `NUMERIC(12, 2)` for money-like values
- `TIMESTAMP`
- Partial indexes with `WHERE`
- `CHECK` constraints

## Future Phase Notes

Possible improvements for later phases:

- Add seed data for lookup tables.
- Add SQL queries required in phase 2.
- Add stored procedures or PostgreSQL functions if required by phase 2.
- Add transaction-safe ticket reservation logic.
- Add backend validation in Java.
- Add Redis for active OTP and caching.
- Add ElasticSearch only in the later search optimization phase.

These are intentionally not included in phase 1.
