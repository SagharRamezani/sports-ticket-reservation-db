# Phase 1 Database Design Report

## Project Overview

This project is a sports match ticket reservation and purchase system.

Users can register, log in, search and filter sport match tickets, reserve tickets, complete local payments, view reservation history, cancel tickets, request refunds, and submit issue reports. Support users can review user reports and problematic reservations.

The final technology stack of the project is PostgreSQL for the database, raw Java with JDBC for later backend phases, and HTML/CSS/Vanilla JavaScript for later frontend phases.

---

## Phase 1 Scope

Phase 1 focuses only on database design and documentation.

Included in this phase:

- ER diagram design
- Initial table structure
- Primary key and foreign key planning
- Relationship cardinalities
- Unique constraints and check constraints
- Normalization review up to 3NF
- Initial indexing strategy
- Database design documentation

Not included in this phase:

- Seed data
- Backend implementation
- Frontend implementation
- API implementation
- Java/JDBC code
- Redis implementation
- Stored procedures
- Analytical queries

---

## Technology Stack

| Layer | Technology |
|---|---|
| Database | PostgreSQL |
| Backend in later phases | Raw Java + JDBC |
| Frontend in later phases | HTML, CSS, Vanilla JavaScript |
| ERD Tool | draw.io / diagrams.net |
| Version Control | Git + GitHub |

---

## ER Diagram

The ER diagram source file is located at:

`database/diagrams/ERD.drawio`

The exported image is located at:

`database/diagrams/ERD.png`

The diagram includes the main entities, important attributes, primary keys, foreign keys, and relationship cardinalities. The ERD uses Crow's Foot style notation to show one-to-many, many-to-many through an associative entity, and optional one-to-one relationships.

---

## Entity Design

The database design includes the main entities required for a sports match ticket reservation system.

### Identity and Location Entities

#### roles

Stores user roles such as normal user and support user.

Important attributes:

- `role_id`
- `role_code`
- `role_name`
- `description`

Design reason:

Roles are separated from users so role values are not repeated directly in the `users` table.

#### users

Stores registered users and support users.

Important attributes:

- `user_id`
- `role_id`
- `city_id`
- `first_name`
- `last_name`
- `email`
- `phone_number`
- `password_hash`
- `account_status`
- `registered_at`

Design reason:

A single `users` table is used for both normal users and support users. The user type is controlled through `role_id`.

#### cities

Stores city and province information.

Important attributes:

- `city_id`
- `city_name`
- `province_name`

Design reason:

Cities are used by users, venues, and teams. Keeping cities in a separate table avoids repeated city and province names.

#### venues

Stores stadiums, halls, and sport event locations.

Important attributes:

- `venue_id`
- `city_id`
- `venue_name`
- `address`
- `total_capacity`
- `venue_type`

Design reason:

Many matches can be held in the same venue, so venue information is stored separately from matches.

---

### Sport and Match Entities

#### sports

Stores sport types such as football, volleyball, and basketball.

Important attributes:

- `sport_id`
- `sport_code`
- `sport_name`
- `description`

Design reason:

Sports are separated so teams and matches can reference a normalized sport entity.

#### teams

Stores sport teams.

Important attributes:

- `team_id`
- `sport_id`
- `city_id`
- `team_name`
- `short_name`

Design reason:

Team names should not be repeated in matches. Each team can be linked to a sport and optionally to a city.

#### matches

Stores sport event and match schedule information.

Important attributes:

- `match_id`
- `sport_id`
- `venue_id`
- `home_team_id`
- `away_team_id`
- `match_title`
- `tournament_name`
- `match_start_time`
- `match_status`

Design reason:

The `matches` table connects sports, venues, and teams. It provides the central event record for ticket sales.

---

### Ticket and Reservation Entities

#### ticket_categories

Stores reusable ticket categories such as normal, special, and VIP.

Important attributes:

- `ticket_category_id`
- `category_code`
- `category_name`
- `description`

Design reason:

Ticket category names are reused by many tickets and should not be stored as repeated text.

#### tickets

Stores individual reservable tickets or seats.

Important attributes:

- `ticket_id`
- `match_id`
- `ticket_category_id`
- `section_name`
- `row_number`
- `seat_number`
- `price`
- `ticket_status`

Design reason:

Each ticket belongs to one match and one ticket category. Seat-related information is stored on the ticket because each ticket represents a reservable seat or ticket.

#### reservations

Stores user ticket reservations.

Important attributes:

- `reservation_id`
- `user_id`
- `ticket_id`
- `reservation_status`
- `reserved_at`
- `expires_at`
- `confirmed_at`
- `cancelled_at`

Design reason:

Reservations connect users to tickets and support temporary reservation behavior before payment.

---

### Payment, Cancellation, and Refund Entities

#### payment_methods

Stores available payment methods.

Important attributes:

- `payment_method_id`
- `method_code`
- `method_name`
- `is_active`

Design reason:

Payment methods are configurable and may change later, so they are stored separately.

#### payments

Stores payment attempts and transaction information.

Important attributes:

- `payment_id`
- `reservation_id`
- `user_id`
- `payment_method_id`
- `amount`
- `payment_status`
- `transaction_reference`
- `paid_at`

Design reason:

Payments are separated from reservations because a reservation may need payment tracking, transaction status, and audit information.

#### cancellation_policies

Stores cancellation penalty rules.

Important attributes:

- `cancellation_policy_id`
- `match_id`
- `ticket_category_id`
- `hours_before_match`
- `penalty_percent`
- `description`

Design reason:

Cancellation rules can depend on the match and ticket category. Keeping them separate avoids repeating penalty rules in tickets or reservations.

#### refunds

Stores refund requests and processing results.

Important attributes:

- `refund_id`
- `reservation_id`
- `payment_id`
- `amount`
- `penalty_amount`
- `refund_status`
- `requested_at`
- `processed_at`

Design reason:

Refunds are separated from payments because refund processing has its own status, amount, penalty amount, and timestamps.

---

### Report and Support Entities

#### report_categories

Stores report categories such as payment issue, seat issue, cancellation issue, and match time change.

Important attributes:

- `report_category_id`
- `category_code`
- `category_name`
- `is_active`

Design reason:

Report categories are reusable lookup values.

#### reports

Stores user-submitted reports.

Important attributes:

- `report_id`
- `reporter_user_id`
- `reservation_id`
- `ticket_id`
- `report_category_id`
- `report_title`
- `report_text`
- `report_status`

Design reason:

Reports can be related to tickets or reservations and can later be reviewed by support users.

#### support_actions

Stores support staff actions.

Important attributes:

- `support_action_id`
- `report_id`
- `reservation_id`
- `support_user_id`
- `action_type`
- `action_note`
- `created_at`

Design reason:

Support actions are separated from reports because a report or reservation can have multiple support actions over time.

---

### OTP and Feature Entities

#### otp_logs

Stores OTP audit records.

Important attributes:

- `otp_log_id`
- `user_id`
- `email`
- `phone_number`
- `otp_code_hash`
- `otp_purpose`
- `otp_status`
- `expires_at`
- `verified_at`

Design reason:

The table stores OTP metadata and hashed OTP values for audit purposes. Active OTP storage can be handled with Redis in later phases.

#### features

Stores reusable ticket features such as parking, VIP service, covered stand, dedicated entrance, and catering.

Important attributes:

- `feature_id`
- `feature_code`
- `feature_name`
- `description`

Design reason:

Features are separated because a ticket can have multiple features and a feature can be reused by many tickets.

#### ticket_features

Associative entity between tickets and features.

Important attributes:

- `ticket_id`
- `feature_id`
- `created_at`

Design reason:

This table resolves the many-to-many relationship between `tickets` and `features`.

---

### Sport-Specific Detail Entities

#### football_details

Stores football-specific ticket details.

Important attributes:

- `football_detail_id`
- `ticket_id`
- `league_name`
- `stadium_name`
- `gate_number`
- `stand_number`
- `row_number`
- `seat_number`
- `ticket_type`

#### volleyball_details

Stores volleyball-specific ticket details.

Important attributes:

- `volleyball_detail_id`
- `ticket_id`
- `league_name`
- `hall_name`
- `gate_number`
- `stand_number`
- `row_number`
- `seat_number`
- `ticket_type`

#### basketball_details

Stores basketball-specific ticket details.

Important attributes:

- `basketball_detail_id`
- `ticket_id`
- `league_name`
- `hall_name`
- `gate_number`
- `stand_number`
- `row_number`
- `seat_number`
- `ticket_type`

Design reason:

Sport-specific details are stored in separate tables to avoid adding many nullable sport-specific columns to the main `tickets` table.

---

## Relationship Design

The main relationships in the ERD are:

- One role can belong to many users.
- One city can have many users.
- One city can have many venues.
- One city can have many teams.
- One sport can have many teams.
- One sport can have many matches.
- One venue can host many matches.
- One team can participate in many matches as the home team.
- One team can participate in many matches as the away team.
- One match can have many tickets.
- One ticket category can be used by many tickets.
- One user can have many reservations.
- One ticket can have many reservation records over time.
- One reservation can have many payment records.
- One payment method can be used by many payments.
- One match can have many cancellation policies.
- One ticket category can have many cancellation policies.
- One reservation can have many refund records.
- One payment can be related to many refund records.
- One user can submit many reports.
- One report category can be used by many reports.
- One reservation can have many reports.
- One ticket can have many reports.
- One report can have many support actions.
- One reservation can have many support actions.
- One support user can perform many support actions.
- One user can have many OTP log records.
- Tickets and features have a many-to-many relationship through `ticket_features`.
- One ticket can have zero or one football detail row.
- One ticket can have zero or one volleyball detail row.
- One ticket can have zero or one basketball detail row.

---

## Normalization Summary

The schema is designed to be close to third normal form.

### First Normal Form

All fields are atomic. Multi-valued data is not stored as comma-separated text.

Example:

Ticket features are not stored inside the `tickets` table as text. Instead, the design uses:

- `features`
- `ticket_features`

### Second Normal Form

Many-to-many relationships are separated into junction tables.

Example:

The `ticket_features` table uses a composite key consisting of:

- `ticket_id`
- `feature_id`

This prevents duplicate feature assignments for the same ticket.

### Third Normal Form

Lookup and reference data are separated into their own tables.

Examples:

- Role data is stored in `roles`.
- City and province data are stored in `cities`.
- Sport names are stored in `sports`.
- Ticket category names are stored in `ticket_categories`.
- Payment method names are stored in `payment_methods`.
- Report category names are stored in `report_categories`.

This design reduces redundancy and helps prevent update anomalies.

---

## Constraints Summary

The schema uses several types of constraints.

### Primary Keys

Every main table has a primary key.

### Foreign Keys

Foreign keys preserve relationships between entities such as users, reservations, tickets, matches, payments, reports, and support actions.

### Unique Constraints

Unique constraints are used for natural identifiers and lookup codes, including:

- role codes
- city and province pairs
- sport codes
- ticket category codes
- payment method codes
- report category codes
- feature codes
- user email
- user phone number
- payment transaction reference
- ticket ID in each sport-specific detail table

### Check Constraints

Check constraints are used to validate business rules, including:

- each user must have at least one contact method
- venue capacity must be positive if provided
- match home team and away team cannot be the same
- ticket price cannot be negative
- reservation expiration time must be after reservation time
- payment amount cannot be negative
- refund amount and penalty amount cannot be negative
- penalty percentage must be between 0 and 100
- status fields must use allowed values
- reports must reference at least one related ticket or reservation
- support actions must reference at least one related report or reservation
- OTP logs must reference at least one identity value

---

## Indexing Strategy

The initial indexing strategy supports common search and workflow operations expected in later phases.

Indexes are planned for:

- searching users by role, city, and status
- searching matches by sport, venue, teams, status, and start time
- finding tickets by match, category, status, and price
- listing reservations by user, ticket, status, and expiration time
- finding expired pending reservations
- listing payments by user, reservation, status, and time
- listing cancellation policies by match, ticket category, and time before match
- listing refunds by reservation, payment, status, and request time
- listing reports by reporter, category, status, ticket, reservation, and creation time
- finding support actions by report, reservation, support user, and creation time
- finding OTP logs by user, email, phone number, status, purpose, and expiration time
- finding sport-specific detail rows by ticket and league name

PostgreSQL automatically creates indexes for primary keys and unique constraints, so duplicate indexes are avoided.

---

## Design Decisions

### Single users table

The design uses one `users` table for both normal users and support users. User type is controlled by `role_id`.

Reason:

This avoids duplicate account tables and keeps authentication data centralized.

### Separate lookup tables

Lookup tables are used for roles, cities, sports, ticket categories, payment methods, report categories, and features.

Reason:

This reduces repeated text values and supports future expansion.

### Individual ticket records

Each row in `tickets` represents an individual reservable ticket or seat.

Reason:

This makes seat-level reservation and ticket status tracking easier.

### Junction table for ticket features

The `ticket_features` table is used for the many-to-many relationship between tickets and features.

Reason:

A ticket can have many features, and each feature can be assigned to many tickets.

### Separate sport-specific details

Football, volleyball, and basketball details are stored in separate tables.

Reason:

This avoids adding many nullable sport-specific columns to the main `tickets` table.

### Separate payment and refund tables

Payments and refunds are stored separately from reservations.

Reason:

Payments and refunds have their own statuses, timestamps, amounts, and audit requirements.

### Separate support actions

Support actions are stored separately from reports and reservations.

Reason:

A report or reservation can have multiple support actions over time.

### CHECK constraints for status fields

Status fields are stored as text columns with CHECK constraints.

Reason:

This keeps the phase 1 design simple while still preventing invalid status values.

---

## File Ownership and Team Collaboration

The team follows a low-conflict workflow.

- Saghar owns the main schema files.
- Sarina owns normalization review and table relations.
- Shamim owns the ERD and phase 1 report.

This report and ERD do not directly modify the SQL schema files.

---

## Final Phase 1 Checklist

- [ ] ERD source file exists at `database/diagrams/ERD.drawio`.
- [ ] ERD PNG export exists at `database/diagrams/ERD.png`.
- [ ] ERD includes all required entities.
- [ ] ERD shows primary keys.
- [ ] ERD shows foreign keys.
- [ ] ERD shows one-to-many relationships.
- [ ] ERD shows the many-to-many relationship between tickets and features through `ticket_features`.
- [ ] ERD shows optional one-to-one relationships for sport-specific detail tables.
- [ ] Report includes project overview.
- [ ] Report includes phase 1 scope.
- [ ] Report includes entity explanations.
- [ ] Report includes relationship explanations.
- [ ] Report includes normalization summary.
- [ ] Report includes constraints summary.
- [ ] Report includes indexing strategy.
- [ ] Report includes design decisions.
- [ ] Report stays within phase 1 scope.

---

## Conclusion

The phase 1 database design provides a normalized relational structure for the sports ticket reservation system.

The ERD includes the main entities, relationships, primary keys, foreign keys, and cardinalities required for later implementation. The design separates lookup data, transaction data, support actions, reports, payment/refund records, OTP logs, ticket features, and sport-specific ticket details to reduce redundancy and support maintainability in future phases.
