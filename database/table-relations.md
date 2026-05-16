# Table Relations and Cardinalities - Phase 1

## Purpose

This document describes the relationships between database tables for the sports ticket reservation and purchase system.

It is intended to help the ERD owner draw an accurate ER diagram and help the schema owner verify foreign keys and cardinalities.

---

## Main Relationships

### User and Role

roles 1 ---- * users

Explanation:
Each user has one role.
Each role can belong to many users.

Suggested FK:
users.role_id -> roles.id

---

### User and City

cities 1 ---- * users

Explanation:
Each user can be associated with one city.
Each city can have many users.

Suggested FK:
users.city_id -> cities.id

---

### City and Venue

cities 1 ---- * venues

Explanation:
Each venue is located in one city.
Each city can have many venues.

Suggested FK:
venues.city_id -> cities.id

---

### Sport and Team

sports 1 ---- * teams

Explanation:
Each team belongs to one sport.
Each sport can have many teams.

Suggested FK:
teams.sport_id -> sports.id

---

### Sport and Match

sports 1 ---- * matches

Explanation:
Each match belongs to one sport.
Each sport can have many matches.

Suggested FK:
matches.sport_id -> sports.id

---

### Venue and Match

venues 1 ---- * matches

Explanation:
Each match is held in one venue.
Each venue can host many matches.

Suggested FK:
matches.venue_id -> venues.id

---

### Team and Match

teams 1 ---- * matches as home_team
teams 1 ---- * matches as away_team

Explanation:
Each match has one home team and one away team.
Each team can participate in many matches.

Suggested FKs:
matches.home_team_id -> teams.id
matches.away_team_id -> teams.id

Important Constraint:
home_team_id <> away_team_id

---

### Match and Ticket

matches 1 ---- * tickets

Explanation:
Each ticket belongs to one match.
Each match can have many ticket records.

Suggested FK:
tickets.match_id -> matches.id

---

### Ticket Category and Ticket

ticket_categories 1 ---- * tickets

Explanation:
Each ticket has one category.
Each category can be used by many tickets.

Suggested FK:
tickets.category_id -> ticket_categories.id

---

### Ticket and Feature

tickets * ---- * features through ticket_features

Explanation:
Each ticket can have many features.
Each feature can be assigned to many tickets.

Suggested FKs:
ticket_features.ticket_id -> tickets.id
ticket_features.feature_id -> features.id

Suggested PK or Unique Constraint:
PRIMARY KEY (ticket_id, feature_id)

---

### User and Reservation

users 1 ---- * reservations

Explanation:
Each reservation belongs to one user.
Each user can have many reservations.

Suggested FK:
reservations.user_id -> users.id

---

### Ticket and Reservation

tickets 1 ---- * reservations

Explanation:
Each reservation is for one ticket record.
Each ticket can have many reservations over time.

Suggested FK:
reservations.ticket_id -> tickets.id

Design Note:
If tickets represent individual physical seats, each ticket should not have more than one active successful reservation.
If tickets represent inventory by category, multiple reservations can point to the same ticket while capacity is controlled by quantity and remaining_capacity.

---

### Reservation and Payment

reservations 1 ---- * payments

Explanation:
A reservation can have zero or more payment attempts.
Each payment belongs to one reservation.

Suggested FK:
payments.reservation_id -> reservations.id

---

### Payment Method and Payment

payment_methods 1 ---- * payments

Explanation:
Each payment uses one payment method.
Each payment method can be used by many payments.

Suggested FK:
payments.payment_method_id -> payment_methods.id

---

### Reservation and Refund

reservations 1 ---- * refunds

Explanation:
A reservation may have zero or more refund records.
Each refund belongs to one reservation.

Suggested FK:
refunds.reservation_id -> reservations.id

---

### User and Report

users 1 ---- * reports

Explanation:
Each report is submitted by one user.
Each user can submit many reports.

Suggested FK:
reports.user_id -> users.id

---

### Report Category and Report

report_categories 1 ---- * reports

Explanation:
Each report has one category.
Each category can be used by many reports.

Suggested FK:
reports.category_id -> report_categories.id

---

### Ticket and Report

tickets 1 ---- * reports

Explanation:
A report may be related to a ticket.
A ticket can have many reports.

Suggested FK:
reports.ticket_id -> tickets.id

Optional:
reports.ticket_id can be nullable if the report is related only to a reservation, payment, or match.

---

### Reservation and Report

reservations 1 ---- * reports

Explanation:
A report may be related to a reservation.
A reservation can have many reports.

Suggested FK:
reports.reservation_id -> reservations.id

Optional:
reports.reservation_id can be nullable if the report is related only to a ticket, payment, or match.

---

### Support User and Support Action

users 1 ---- * support_actions as support_user

Explanation:
A support action is performed by one support user.
Each support user can perform many support actions.

Suggested FK:
support_actions.support_user_id -> users.id

Important Constraint:
support_user_id should reference a user whose role is support/admin.

---

### Report and Support Action

reports 1 ---- * support_actions

Explanation:
A support action may be taken for a report.
Each report can have many support actions.

Suggested FK:
support_actions.report_id -> reports.id

Optional:
support_actions.report_id can be nullable if the action is related to a reservation or payment instead.

---

### Reservation and Support Action

reservations 1 ---- * support_actions

Explanation:
A support action may be taken for a reservation.
Each reservation can have many support actions.

Suggested FK:
support_actions.reservation_id -> reservations.id

Optional:
support_actions.reservation_id can be nullable.

---

### User and OTP Logs

users 1 ---- * otp_logs

Explanation:
A user can have many OTP log records.
Each OTP log belongs to one user if the user already exists.

Suggested FK:
otp_logs.user_id -> users.id

Optional:
otp_logs.user_id can be nullable if OTP is requested before signup.

---

### Ticket and Sport-Specific Details

tickets 1 ---- 0..1 football_details
tickets 1 ---- 0..1 volleyball_details
tickets 1 ---- 0..1 basketball_details

Explanation:
A ticket can have at most one sport-specific detail record.
Only one of these detail tables should apply depending on the sport of the related match.

Suggested FKs:
football_details.ticket_id -> tickets.id
volleyball_details.ticket_id -> tickets.id
basketball_details.ticket_id -> tickets.id

Suggested Constraints:
UNIQUE (ticket_id) in each detail table.

---

## Relationship Summary

roles 1 ---- * users
cities 1 ---- * users
cities 1 ---- * venues
sports 1 ---- * teams
sports 1 ---- * matches
venues 1 ---- * matches
teams 1 ---- * matches as home_team
teams 1 ---- * matches as away_team
matches 1 ---- * tickets
ticket_categories 1 ---- * tickets
tickets * ---- * features through ticket_features
users 1 ---- * reservations
tickets 1 ---- * reservations
reservations 1 ---- * payments
payment_methods 1 ---- * payments
reservations 1 ---- * refunds
users 1 ---- * reports
report_categories 1 ---- * reports
tickets 1 ---- * reports
reservations 1 ---- * reports
users 1 ---- * support_actions as support_user
reports 1 ---- * support_actions
reservations 1 ---- * support_actions
users 1 ---- * otp_logs
tickets 1 ---- 0..1 football_details
tickets 1 ---- 0..1 volleyball_details
tickets 1 ---- 0..1 basketball_details

---

## ERD Notes for Shamim

1. Show `ticket_features` as a junction table between `tickets` and `features`.
2. Show two separate relationships from `teams` to `matches`:
   - `home_team_id`
   - `away_team_id`
3. Show sport-specific detail tables as optional one-to-one tables with `tickets`.
4. Show `payment_methods` and `report_categories` as lookup/reference tables.
5. Show `roles` and `cities` as lookup/reference tables.
6. Show `support_actions` connected to `users` as support users.
7. Use nullable relationships for reports if a report can refer to ticket, reservation, payment, or match depending on issue type.

---

## Important Constraints to Show Near ERD

- `matches.home_team_id <> matches.away_team_id`
- `reservations.expires_at > reservations.reserved_at`
- `tickets.price >= 0`
- `payments.amount >= 0`
- `refunds.amount >= 0`
- `tickets.remaining_capacity >= 0`
- `tickets.remaining_capacity <= tickets.total_capacity`
- `ticket_features` should have `PRIMARY KEY (ticket_id, feature_id)`
- Sport-specific detail tables should have `UNIQUE (ticket_id)`
---

# Updated Relation Notes Based on Saghar Schema

This section aligns the relationship document with the current schema files.

---

## Actual Column Names Used in Schema

### roles to users

roles 1 ---- * users

Actual FK:
`users.role_id -> roles.role_id`

---

### cities to users

cities 1 ---- * users

Actual FK:
`users.city_id -> cities.city_id`

---

### cities to venues

cities 1 ---- * venues

Actual FK:
`venues.city_id -> cities.city_id`

---

### cities to teams

cities 1 ---- * teams

Actual FK:
`teams.city_id -> cities.city_id`

Note:
`teams.city_id` is nullable.

---

### sports to teams

sports 1 ---- * teams

Actual FK:
`teams.sport_id -> sports.sport_id`

---

### sports to matches

sports 1 ---- * matches

Actual FK:
`matches.sport_id -> sports.sport_id`

---

### venues to matches

venues 1 ---- * matches

Actual FK:
`matches.venue_id -> venues.venue_id`

---

### teams to matches

teams 1 ---- * matches as home_team
teams 1 ---- * matches as away_team

Actual FKs:
`matches.home_team_id -> teams.team_id`
`matches.away_team_id -> teams.team_id`

Note:
Both team fields are nullable in the current schema to support events where teams are unknown or not applicable.

Constraint:
`home_team_id IS NULL OR away_team_id IS NULL OR home_team_id <> away_team_id`

---

### matches to tickets

matches 1 ---- * tickets

Actual FK:
`tickets.match_id -> matches.match_id`

Note:
In Saghar's schema, each ticket represents an individual reservable seat or ticket.

---

### ticket_categories to tickets

ticket_categories 1 ---- * tickets

Actual FK:
`tickets.ticket_category_id -> ticket_categories.ticket_category_id`

---

### users to reservations

users 1 ---- * reservations

Actual FK:
`reservations.user_id -> users.user_id`

---

### tickets to reservations

tickets 1 ---- * reservations

Actual FK:
`reservations.ticket_id -> tickets.ticket_id`

Important ERD Note:
Because `tickets` represents individual seats, the ERD should show that one ticket can have many reservation records over time, but business rules should prevent more than one active reservation for the same ticket.

---

### reservations to payments

reservations 1 ---- * payments

Actual FK:
`payments.reservation_id -> reservations.reservation_id`

Note:
One reservation can have multiple payment records or attempts.

---

### users to payments

users 1 ---- * payments

Actual FK:
`payments.user_id -> users.user_id`

Review Note:
This relationship exists in the current schema. However, the user is also reachable through `payments -> reservations -> users`.

---

### payment_methods to payments

payment_methods 1 ---- * payments

Actual FK:
`payments.payment_method_id -> payment_methods.payment_method_id`

---

### matches to cancellation_policies

matches 1 ---- * cancellation_policies

Actual FK:
`cancellation_policies.match_id -> matches.match_id`

Note:
`match_id` is nullable.

---

### ticket_categories to cancellation_policies

ticket_categories 1 ---- * cancellation_policies

Actual FK:
`cancellation_policies.ticket_category_id -> ticket_categories.ticket_category_id`

Note:
`ticket_category_id` is nullable.

---

### reservations to refunds

reservations 1 ---- * refunds

Actual FK:
`refunds.reservation_id -> reservations.reservation_id`

---

### payments to refunds

payments 1 ---- * refunds

Actual FK:
`refunds.payment_id -> payments.payment_id`

---

### users to reports

users 1 ---- * reports

Actual FK:
`reports.reporter_user_id -> users.user_id`

---

### reservations to reports

reservations 1 ---- * reports

Actual FK:
`reports.reservation_id -> reservations.reservation_id`

Note:
`reservation_id` is nullable, but each report must reference either a reservation or a ticket.

---

### tickets to reports

tickets 1 ---- * reports

Actual FK:
`reports.ticket_id -> tickets.ticket_id`

Note:
`ticket_id` is nullable, but each report must reference either a reservation or a ticket.

---

### report_categories to reports

report_categories 1 ---- * reports

Actual FK:
`reports.report_category_id -> report_categories.report_category_id`

---

### reports to support_actions

reports 1 ---- * support_actions

Actual FK:
`support_actions.report_id -> reports.report_id`

Note:
`report_id` is nullable, but each support action must reference either a report or a reservation.

---

### reservations to support_actions

reservations 1 ---- * support_actions

Actual FK:
`support_actions.reservation_id -> reservations.reservation_id`

Note:
`reservation_id` is nullable, but each support action must reference either a report or a reservation.

---

### users to support_actions

users 1 ---- * support_actions as support_user

Actual FK:
`support_actions.support_user_id -> users.user_id`

Note:
The support role is determined through `users.role_id`.

---

### users to otp_logs

users 1 ---- * otp_logs

Actual FK:
`otp_logs.user_id -> users.user_id`

Note:
`user_id` is nullable because OTP may be requested before signup or by email/phone.

---

### tickets to features

tickets * ---- * features through ticket_features

Actual FKs:
`ticket_features.ticket_id -> tickets.ticket_id`
`ticket_features.feature_id -> features.feature_id`

Actual PK:
`PRIMARY KEY (ticket_id, feature_id)`

---

### tickets to football_details

tickets 1 ---- 0..1 football_details

Actual FK:
`football_details.ticket_id -> tickets.ticket_id`

Actual Unique Constraint:
`UNIQUE (ticket_id)`

---

### tickets to volleyball_details

tickets 1 ---- 0..1 volleyball_details

Actual FK:
`volleyball_details.ticket_id -> tickets.ticket_id`

Actual Unique Constraint:
`UNIQUE (ticket_id)`

---

### tickets to basketball_details

tickets 1 ---- 0..1 basketball_details

Actual FK:
`basketball_details.ticket_id -> tickets.ticket_id`

Actual Unique Constraint:
`UNIQUE (ticket_id)`

---

## Updated ERD Notes for Shamim

1. Use the actual primary key names from Saghar's schema:
   - `user_id`
   - `ticket_id`
   - `match_id`
   - `reservation_id`
   - etc.

2. Show `payments.user_id` as a direct relationship to `users`, but add a note that the user is also reachable through `reservation_id`.

3. Show `tickets` as individual reservable tickets/seats, not as capacity groups.

4. Do not draw `total_capacity` or `remaining_capacity` on `tickets`, because Saghar's current design does not use capacity-based ticket groups.

5. Show nullable team relationships in `matches`:
   - `home_team_id` can be null.
   - `away_team_id` can be null.

6. Show `cancellation_policies` connected to both `matches` and `ticket_categories`.

7. Show `reports` connected to both `reservations` and `tickets`, with a note that at least one is required.

8. Show `support_actions` connected to both `reports` and `reservations`, with a note that at least one is required.

9. Show `football_details`, `volleyball_details`, and `basketball_details` as optional one-to-one relationships with `tickets`.

10. Show `ticket_features` as the junction table between `tickets` and `features`.
