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
