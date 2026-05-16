# Normalization Review - Phase 1

## Purpose

This document reviews the proposed database design for the sports ticket reservation and purchase system.

The goal is to check whether the schema follows normalization rules, especially 1NF, 2NF, and 3NF, and to identify possible redundancy without directly changing the SQL schema file.

Schema ownership note:
The SQL schema is owned by Saghar. This document only contains review notes and suggestions.

---

## Phase 1 Scope

This review focuses only on:

- Table responsibilities
- Relationships
- Cardinalities
- Primary keys and foreign keys
- Basic constraints
- Normalization up to 3NF
- Redundancy risks

This document does not cover:

- Seed data
- Analytical queries
- Backend implementation
- Java/JDBC implementation
- Frontend implementation

---

## 1NF Review

A table is in 1NF when each column contains atomic values and there are no repeating groups.

The proposed design can satisfy 1NF if:

- Features are not stored as comma-separated text.
- Ticket features are stored through `features` and `ticket_features`.
- Home team and away team are stored as separate foreign keys.
- Seat, row, section, and stand information are stored in separate atomic columns.
- Each table has a primary key.

Conclusion:
The design is compatible with 1NF if multi-valued attributes are modeled using separate tables.

---

## 2NF Review

A table is in 2NF when it is already in 1NF and every non-key attribute depends on the whole primary key.

Most tables use a single-column primary key, so 2NF is mostly satisfied automatically.

For the junction table `ticket_features`, the composite key can be:

`(ticket_id, feature_id)`

This table should not store attributes such as `feature_name` or `ticket_price`, because those depend only on one side of the relationship.

Conclusion:
The design satisfies 2NF if junction tables only store relationship-specific attributes.

---

## 3NF Review

A table is in 3NF when it is already in 2NF and non-key attributes do not depend on other non-key attributes.

The design can satisfy 3NF if:

- `users` stores `role_id` instead of `role_name`.
- `users` stores `city_id` instead of `city_name`.
- `venues` references `cities`.
- `matches` references `sports`, `venues`, `home_team_id`, and `away_team_id`.
- `tickets` references `matches` and `ticket_categories`.
- `payments` references `payment_methods`.
- `reports` references `report_categories`.
- Sport-specific details do not repeat venue, sport, price, or category information.

Conclusion:
The schema can satisfy 3NF if derived or repeated fields are not stored in transactional tables.

---

## Why Some Tables Are Separated

### roles

`roles` is separate because many users can have the same role. This avoids repeating role names inside `users`.

### cities

`cities` is separate because both users and venues can reference cities. This avoids repeated city names and supports city-based filtering.

### features

`features` is separate because each ticket can have multiple features and each feature can be reused by many tickets.

### payment_methods

`payment_methods` is separate because many payments can use the same method.

### report_categories

`report_categories` is separate because many reports can belong to the same category.

### football_details, volleyball_details, basketball_details

Sport-specific detail tables are separate to avoid adding many nullable sport-specific columns to the main `tickets` table.

---

## Initial Suggestions for Schema Owner

### Suggested Fix 1

Suggested Fix:
Use foreign keys instead of repeated names for roles, cities, sports, payment methods, and report categories.

Reason:
This prevents redundancy and supports 3NF.

Affected Tables:
`users`, `venues`, `matches`, `payments`, `reports`

---

### Suggested Fix 2

Suggested Fix:
Use `features` and `ticket_features` instead of storing features as text or JSON in `tickets`.

Reason:
A ticket can have many features, and a feature can belong to many tickets.

Affected Tables:
`tickets`, `features`, `ticket_features`

---

### Suggested Fix 3

Suggested Fix:
Avoid repeating venue name, sport name, ticket category name, and price in sport-specific detail tables.

Reason:
These values can be reached through `ticket -> match -> venue/sport` and `ticket -> category`.

Affected Tables:
`football_details`, `volleyball_details`, `basketball_details`

---

## Initial Checklist

- [ ] No comma-separated values are stored in columns.
- [ ] Lookup values are separated into reference tables.
- [ ] Many-to-many relationships use junction tables.
- [ ] Foreign keys are used instead of repeated names.
- [ ] Price and amount fields have non-negative constraints.
- [ ] Reservation expiration time is after reservation time.
- [ ] Sport-specific detail tables do not duplicate general ticket or match data.
# Schema Improvement Suggestions for Schema Owner

This section contains suggested improvements for the SQL schema owner.
These are review notes only. The SQL schema file should not be changed directly by Sarina.

---

## Suggested Fix 1: Use foreign keys instead of repeated names

Suggested Fix:
Use foreign keys for lookup/reference data instead of storing repeated text values.

Examples:
- `users.role_id` instead of `users.role_name`
- `users.city_id` instead of `users.city_name`
- `venues.city_id` instead of `venues.city_name`
- `matches.sport_id` instead of `matches.sport_name`
- `payments.payment_method_id` instead of `payments.payment_method_name`
- `reports.category_id` instead of `reports.category_name`

Reason:
Repeated text values may cause inconsistency and violate 3NF. Reference tables keep data consistent and reduce redundancy.

Affected Tables:
`users`, `venues`, `matches`, `payments`, `reports`, `roles`, `cities`, `sports`, `payment_methods`, `report_categories`

---

## Suggested Fix 2: Add unique constraints to lookup tables

Suggested Fix:
Add `UNIQUE` constraints to lookup/reference table names.

Suggested Constraints:
- `roles.name` should be unique.
- `sports.name` should be unique.
- `ticket_categories.name` should be unique.
- `features.name` should be unique.
- `payment_methods.name` should be unique.
- `report_categories.name` should be unique.

Reason:
Lookup tables should not contain duplicate values. For example, having two rows for `VIP` or two rows for `football` can create inconsistent references.

Affected Tables:
`roles`, `sports`, `ticket_categories`, `features`, `payment_methods`, `report_categories`

---

## Suggested Fix 3: Add check constraints for numeric values

Suggested Fix:
Add check constraints to prevent invalid negative values.

Suggested Constraints:
- `tickets.price >= 0`
- `tickets.total_capacity >= 0`
- `tickets.remaining_capacity >= 0`
- `tickets.remaining_capacity <= tickets.total_capacity`
- `payments.amount >= 0`
- `refunds.amount >= 0`

Reason:
Price, capacity, payment amount, and refund amount cannot be negative. Remaining capacity also cannot be greater than total capacity.

Affected Tables:
`tickets`, `payments`, `refunds`

---

## Suggested Fix 4: Add check constraint for reservation expiration

Suggested Fix:
Add a constraint to ensure that reservation expiration time is after reservation creation time.

Suggested Constraint:
`reservations.expires_at > reservations.reserved_at`

Reason:
A reservation cannot expire before it is created.

Affected Tables:
`reservations`

---

## Suggested Fix 5: Prevent a team from playing against itself

Suggested Fix:
Add a check constraint on the `matches` table.

Suggested Constraint:
`matches.home_team_id <> matches.away_team_id`

Reason:
A match should not have the same team as both home team and away team.

Affected Tables:
`matches`

---

## Suggested Fix 6: Use ticket_features as a junction table

Suggested Fix:
Use `ticket_features` to model the many-to-many relationship between tickets and features.

Suggested Constraint:
`PRIMARY KEY (ticket_id, feature_id)`

Reason:
A ticket can have multiple features, and one feature can belong to multiple tickets. This should not be stored as comma-separated text or repeated columns.

Affected Tables:
`tickets`, `features`, `ticket_features`

---

## Suggested Fix 7: Avoid duplicate data in sport-specific detail tables

Suggested Fix:
Do not repeat general ticket, match, venue, sport, or category information in sport-specific detail tables.

Avoid storing:
- `venue_name`
- `sport_name`
- `ticket_category_name`
- `match_datetime`
- `price`

Reason:
These values can already be reached through existing relationships:

`football_details -> tickets -> matches -> venues`
`football_details -> tickets -> matches -> sports`
`football_details -> tickets -> ticket_categories`

The same logic applies to volleyball and basketball details.

Affected Tables:
`football_details`, `volleyball_details`, `basketball_details`, `tickets`, `matches`, `venues`, `sports`, `ticket_categories`

---

## Suggested Fix 8: Add unique ticket constraint to sport-specific detail tables

Suggested Fix:
Add `UNIQUE (ticket_id)` to each sport-specific detail table.

Reason:
Each ticket should have at most one football, volleyball, or basketball detail record.

Affected Tables:
`football_details`, `volleyball_details`, `basketball_details`

---

## Suggested Fix 9: Clarify the meaning of tickets

Suggested Fix:
The team should decide whether the `tickets` table represents:

1. Individual physical seats/tickets
2. Ticket inventory groups by match, category, section, and capacity

Reason:
This decision affects the design of reservations, quantity, capacity, and seat-specific details.

If `tickets` means individual physical seats:
- Each ticket should have only one successful reservation.
- `quantity` in reservations may not be needed.
- Seat number should probably be unique per match/section.

If `tickets` means inventory groups:
- `quantity` in reservations is useful.
- `total_capacity` and `remaining_capacity` are useful.
- Multiple reservations can reference the same ticket record.

Recommendation:
For phase 1, using `tickets` as inventory groups by match/category/section is simpler and better aligned with capacity management.

Affected Tables:
`tickets`, `reservations`, `football_details`, `volleyball_details`, `basketball_details`

---

## Suggested Fix 10: Add email or phone requirement for users

Suggested Fix:
Add a check constraint to ensure that each user has at least one login/contact method.

Suggested Constraint:
`email IS NOT NULL OR phone_number IS NOT NULL`

Reason:
The system supports login and OTP using email or phone number, so at least one of them should exist.

Affected Tables:
`users`

---

## Suggested Fix 11: Use controlled values for status columns

Suggested Fix:
Use `CHECK` constraints or separate lookup tables for status fields.

Suggested Status Values:

For reservations:
- `reserved`
- `paid`
- `canceled`
- `expired`

For payments:
- `pending`
- `successful`
- `failed`
- `refunded`

For reports:
- `pending`
- `in_review`
- `resolved`
- `rejected`

For refunds:
- `pending`
- `processed`
- `failed`

Reason:
Unrestricted text status fields can cause inconsistent values such as `success`, `successful`, `done`, or `paid_successfully`.

Affected Tables:
`reservations`, `payments`, `reports`, `refunds`

---

## Suggested Fix 12: Keep OTP codes secure

Suggested Fix:
Do not store raw OTP codes in the relational database. If OTP logging is needed, store only metadata or hashed OTP values.

Reason:
Raw OTP codes are sensitive authentication data. Active OTP validation can be handled later by Redis, while the relational table can keep only audit metadata.

Affected Tables:
`otp_logs`

---

# Summary of Suggestions

The most important schema improvements are:

1. Use foreign keys instead of repeated names.
2. Add unique constraints to lookup tables.
3. Add check constraints for price, capacity, amount, and reservation expiration.
4. Use `ticket_features` for the many-to-many relationship between tickets and features.
5. Avoid duplicate general data in sport-specific detail tables.
6. Clarify whether `tickets` means physical seats or inventory groups.
7. Use controlled values for status columns.

# Final Phase 1 Checklist

This checklist is used to verify the phase 1 database design before final review and merge.

---

## Entity Checklist

- [ ] `roles` table exists.
- [ ] `users` table exists.
- [ ] `users` references `roles`.
- [ ] `users` references `cities`.
- [ ] `cities` table exists.
- [ ] `venues` table exists.
- [ ] `venues` references `cities`.
- [ ] `sports` table exists.
- [ ] `teams` table exists.
- [ ] `teams` references `sports`.
- [ ] `matches` table exists.
- [ ] `matches` references `sports`.
- [ ] `matches` references `venues`.
- [ ] `matches` references home team and away team.
- [ ] `ticket_categories` table exists.
- [ ] `tickets` table exists.
- [ ] `tickets` references `matches`.
- [ ] `tickets` references `ticket_categories`.
- [ ] `features` table exists.
- [ ] `ticket_features` table exists.
- [ ] `ticket_features` resolves the many-to-many relationship between `tickets` and `features`.
- [ ] `reservations` table exists.
- [ ] `reservations` references `users`.
- [ ] `reservations` references `tickets`.
- [ ] `payment_methods` table exists.
- [ ] `payments` table exists.
- [ ] `payments` references `reservations`.
- [ ] `payments` references `payment_methods`.
- [ ] `cancellation_policies` table exists.
- [ ] `refunds` table exists.
- [ ] `refunds` references `reservations`.
- [ ] `report_categories` table exists.
- [ ] `reports` table exists.
- [ ] `reports` references `users`.
- [ ] `reports` references `report_categories`.
- [ ] `support_actions` table exists.
- [ ] `support_actions` references support users.
- [ ] `otp_logs` table exists if relational OTP logging is needed.
- [ ] `football_details` references `tickets`.
- [ ] `volleyball_details` references `tickets`.
- [ ] `basketball_details` references `tickets`.

---

## Relationship Checklist

- [ ] `roles 1 ---- * users`
- [ ] `cities 1 ---- * users`
- [ ] `cities 1 ---- * venues`
- [ ] `sports 1 ---- * teams`
- [ ] `sports 1 ---- * matches`
- [ ] `venues 1 ---- * matches`
- [ ] `teams 1 ---- * matches as home_team`
- [ ] `teams 1 ---- * matches as away_team`
- [ ] `matches 1 ---- * tickets`
- [ ] `ticket_categories 1 ---- * tickets`
- [ ] `tickets * ---- * features through ticket_features`
- [ ] `users 1 ---- * reservations`
- [ ] `tickets 1 ---- * reservations`
- [ ] `reservations 1 ---- * payments`
- [ ] `payment_methods 1 ---- * payments`
- [ ] `reservations 1 ---- * refunds`
- [ ] `users 1 ---- * reports`
- [ ] `report_categories 1 ---- * reports`
- [ ] `tickets 1 ---- * reports`
- [ ] `reservations 1 ---- * reports`
- [ ] `users 1 ---- * support_actions as support_user`
- [ ] `reports 1 ---- * support_actions`
- [ ] `reservations 1 ---- * support_actions`
- [ ] `users 1 ---- * otp_logs`
- [ ] `tickets 1 ---- 0..1 football_details`
- [ ] `tickets 1 ---- 0..1 volleyball_details`
- [ ] `tickets 1 ---- 0..1 basketball_details`

---

## 1NF Checklist

- [ ] Each table has a primary key.
- [ ] Each column stores atomic values.
- [ ] There are no comma-separated values in columns.
- [ ] There are no repeated columns such as `feature1`, `feature2`, `feature3`.
- [ ] Home team and away team are stored separately.
- [ ] Seat, row, section, and stand information are stored in separate fields.
- [ ] Many-to-many relationships use junction tables.

---

## 2NF Checklist

- [ ] All tables are already in 1NF.
- [ ] Non-key attributes depend on the whole primary key.
- [ ] Junction tables do not store data that depends only on one side of the relationship.
- [ ] `ticket_features` does not store `feature_name`.
- [ ] `ticket_features` does not store `ticket_price`.
- [ ] Composite keys, if used, do not create partial dependency.

---

## 3NF Checklist

- [ ] All tables are already in 2NF.
- [ ] `users` does not store `role_name`.
- [ ] `users` does not store `city_name`.
- [ ] `venues` does not duplicate city/province data unnecessarily.
- [ ] `matches` does not store team names as text.
- [ ] `matches` does not store venue name as text.
- [ ] `tickets` does not store sport name.
- [ ] `tickets` does not store venue name.
- [ ] `tickets` does not store match datetime if it already exists in `matches`.
- [ ] `tickets` does not store category name directly.
- [ ] `payments` does not store payment method name directly.
- [ ] `reports` does not store report category name directly.
- [ ] Sport-specific detail tables do not duplicate venue, sport, price, category, or match datetime.

---

## Constraint Checklist

- [ ] `users.email` is unique if used.
- [ ] `users.phone_number` is unique if used.
- [ ] `users` has a rule that at least one of email or phone number must exist.
- [ ] `roles.name` is unique.
- [ ] `sports.name` is unique.
- [ ] `ticket_categories.name` is unique.
- [ ] `features.name` is unique.
- [ ] `payment_methods.name` is unique.
- [ ] `report_categories.name` is unique.
- [ ] `matches.home_team_id <> matches.away_team_id`.
- [ ] `tickets.price >= 0`.
- [ ] `tickets.total_capacity >= 0`.
- [ ] `tickets.remaining_capacity >= 0`.
- [ ] `tickets.remaining_capacity <= tickets.total_capacity`.
- [ ] `reservations.quantity > 0` if quantity exists.
- [ ] `reservations.expires_at > reservations.reserved_at`.
- [ ] `payments.amount >= 0`.
- [ ] `refunds.amount >= 0`.
- [ ] `ticket_features` has `PRIMARY KEY (ticket_id, feature_id)` or `UNIQUE (ticket_id, feature_id)`.
- [ ] `football_details` has `UNIQUE (ticket_id)`.
- [ ] `volleyball_details` has `UNIQUE (ticket_id)`.
- [ ] `basketball_details` has `UNIQUE (ticket_id)`.

---

## Redundancy Checklist

- [ ] Role names are not repeated inside `users`.
- [ ] City names are not repeated inside `users` or `venues`.
- [ ] Sport names are not repeated inside `matches` or `tickets`.
- [ ] Team names are not repeated inside `matches` or `tickets`.
- [ ] Venue names are not repeated inside `matches`, `tickets`, or sport-specific detail tables.
- [ ] Ticket category names are not repeated inside `tickets`.
- [ ] Payment method names are not repeated inside `payments`.
- [ ] Report category names are not repeated inside `reports`.
- [ ] Features are not stored as text, JSON, or comma-separated values inside `tickets`.

---

## Final Review Notes

Before merging this branch, the following points should be confirmed with the schema owner:

1. Whether `tickets` represents individual physical seats or inventory groups.
2. Whether status fields use `CHECK` constraints or lookup tables.
3. Whether sport-specific detail tables avoid duplicated general data.
4. Whether all important relationships are visible in the ERD.
5. Whether all suggested constraints are either implemented or intentionally postponed.

---

## Overall Phase 1 Review Result

The proposed database design can satisfy 3NF if repeated names and derived attributes are avoided.

The current review recommends using reference tables, foreign keys, junction tables, and check constraints to keep the schema consistent, normalized, and suitable for the next phases of the project.
