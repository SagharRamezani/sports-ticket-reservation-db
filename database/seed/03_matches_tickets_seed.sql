-- ============================================================
-- 03_matches_tickets_seed.sql
-- Phase 2 - Seed data for ticket categories, matches, tickets,
-- features, ticket features, sport-specific details, and
-- cancellation policies
-- Project: Sports Match Ticket Reservation System
-- Owner: Shamim
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- 1. This file depends on:
--    database/seed/01_users_seed.sql
--    database/seed/02_sports_venues_teams_seed.sql
-- 2. Explicit IDs are used so later reservation/payment/report seeds
--    can reference stable ticket and match IDs.
-- 3. OVERRIDING SYSTEM VALUE is required because the schema uses
--    GENERATED ALWAYS AS IDENTITY.
-- 4. This file includes:
--    - Football, volleyball, and basketball matches
--    - Azadi Stadium matches
--    - VIP, NORMAL, SPECIAL, ECONOMY, FAMILY categories
--    - Different prices
--    - AVAILABLE, RESERVED, SOLD, CANCELLED, and UNAVAILABLE tickets
--    - Ticket features through ticket_features
--    - Sport-specific details for football, volleyball, and basketball
--    - Cancellation policies by match and ticket category

-- ============================================================
-- 1. Ticket Categories
-- ============================================================

INSERT INTO ticket_categories
    (ticket_category_id, category_code, category_name, description, created_at)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'NORMAL', 'Normal', 'Standard ticket category with regular seating.', CURRENT_TIMESTAMP),
    (2, 'VIP', 'VIP', 'VIP ticket category with premium seating and extra services.', CURRENT_TIMESTAMP),
    (3, 'SPECIAL', 'Special', 'Special ticket category with better view or services.', CURRENT_TIMESTAMP),
    (4, 'ECONOMY', 'Economy', 'Low-cost ticket category.', CURRENT_TIMESTAMP),
    (5, 'FAMILY', 'Family', 'Family-friendly ticket category.', CURRENT_TIMESTAMP)
ON CONFLICT (ticket_category_id) DO UPDATE SET
    category_code = EXCLUDED.category_code,
    category_name = EXCLUDED.category_name,
    description = EXCLUDED.description;

-- ============================================================
-- 2. Matches
-- ============================================================

INSERT INTO matches
    (
        match_id,
        sport_id,
        venue_id,
        home_team_id,
        away_team_id,
        match_title,
        tournament_name,
        match_start_time,
        match_status,
        created_at,
        updated_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    -- Football
    (1, 1, 1, 1, 2, 'Tehran Reds FC vs Tehran Blues FC', 'Persian Gulf Pro League',
     CURRENT_TIMESTAMP + INTERVAL '3 days', 'SCHEDULED', CURRENT_TIMESTAMP, NULL),

    (2, 1, 1, 1, 3, 'Tehran Reds FC vs Isfahan Lions FC', 'Hazfi Cup',
     CURRENT_TIMESTAMP + INTERVAL '7 days', 'SCHEDULED', CURRENT_TIMESTAMP, NULL),

    (3, 1, 5, 3, 4, 'Isfahan Lions FC vs Tabriz Eagles FC', 'Persian Gulf Pro League',
     CURRENT_TIMESTAMP - INTERVAL '1 day', 'FINISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (4, 1, 8, 4, 2, 'Tabriz Eagles FC vs Tehran Blues FC', 'Persian Gulf Pro League',
     CURRENT_TIMESTAMP + INTERVAL '12 days', 'POSTPONED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Volleyball
    (5, 2, 2, 5, 6, 'Tehran Spikers VC vs Qom Setters VC', 'National Volleyball League',
     CURRENT_TIMESTAMP + INTERVAL '2 days', 'SCHEDULED', CURRENT_TIMESTAMP, NULL),

    (6, 2, 11, 6, 7, 'Qom Setters VC vs Mashhad Net Stars VC', 'National Volleyball League',
     CURRENT_TIMESTAMP + INTERVAL '9 days', 'SCHEDULED', CURRENT_TIMESTAMP, NULL),

    (7, 2, 9, 8, 5, 'Rasht Waves VC vs Tehran Spikers VC', 'Volleyball Cup',
     CURRENT_TIMESTAMP - INTERVAL '2 days', 'FINISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Basketball
    (8, 3, 12, 9, 10, 'Tehran Hoopers BC vs Ahvaz Dunkers BC', 'National Basketball League',
     CURRENT_TIMESTAMP + INTERVAL '1 day', 'SCHEDULED', CURRENT_TIMESTAMP, NULL),

    (9, 3, 10, 10, 11, 'Ahvaz Dunkers BC vs Shiraz Shooters BC', 'Basketball Cup',
     CURRENT_TIMESTAMP + INTERVAL '6 days', 'SCHEDULED', CURRENT_TIMESTAMP, NULL),

    (10, 3, 4, 12, 9, 'Karaj Falcons BC vs Tehran Hoopers BC', 'National Basketball League',
     CURRENT_TIMESTAMP - INTERVAL '1 day', 'FINISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (match_id) DO UPDATE SET
    sport_id = EXCLUDED.sport_id,
    venue_id = EXCLUDED.venue_id,
    home_team_id = EXCLUDED.home_team_id,
    away_team_id = EXCLUDED.away_team_id,
    match_title = EXCLUDED.match_title,
    tournament_name = EXCLUDED.tournament_name,
    match_start_time = EXCLUDED.match_start_time,
    match_status = EXCLUDED.match_status,
    updated_at = EXCLUDED.updated_at;

-- ============================================================
-- 3. Tickets
-- ============================================================

INSERT INTO tickets
    (
        ticket_id,
        match_id,
        ticket_category_id,
        section_name,
        row_number,
        seat_number,
        price,
        ticket_status,
        created_at,
        updated_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    -- Football tickets
    (1, 1, 1, 'East Stand', 'A1', '001', 350000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP),
    (2, 1, 2, 'VIP West', 'V1', '010', 1200000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP),
    (3, 1, 3, 'Covered Stand', 'C1', '021', 750000.00, 'RESERVED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
    (4, 2, 1, 'East Stand', 'A2', '045', 400000.00, 'AVAILABLE', CURRENT_TIMESTAMP, NULL),
    (5, 2, 2, 'VIP West', 'V2', '011', 1300000.00, 'AVAILABLE', CURRENT_TIMESTAMP, NULL),
    (6, 3, 1, 'North Stand', 'N1', '101', 300000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP),
    (7, 3, 4, 'Economy South', 'S1', '180', 180000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP),
    (8, 4, 1, 'Main Stand', 'M1', '022', 350000.00, 'CANCELLED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP),

    -- Volleyball tickets
    (9, 5, 1, 'Hall A', 'R1', '015', 220000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP),
    (10, 5, 2, 'VIP Court Side', 'V1', '002', 650000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP),
    (11, 5, 3, 'Near Court', 'R2', '018', 380000.00, 'RESERVED', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP),
    (12, 6, 1, 'Main Hall', 'R3', '030', 240000.00, 'AVAILABLE', CURRENT_TIMESTAMP, NULL),
    (13, 6, 5, 'Family Area', 'F1', '008', 300000.00, 'AVAILABLE', CURRENT_TIMESTAMP, NULL),
    (14, 7, 1, 'Rasht Hall', 'R4', '041', 210000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP),

    -- Basketball tickets
    (15, 8, 1, 'Arena Lower', 'L1', '006', 250000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
    (16, 8, 2, 'VIP Court Side', 'V1', '001', 800000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
    (17, 8, 3, 'Premium Lower', 'P1', '014', 450000.00, 'RESERVED', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP),
    (18, 9, 1, 'Ahvaz Arena', 'A1', '020', 230000.00, 'AVAILABLE', CURRENT_TIMESTAMP, NULL),
    (19, 9, 2, 'VIP Arena', 'V2', '004', 700000.00, 'UNAVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (20, 10, 1, 'Karaj Arena', 'K1', '033', 240000.00, 'SOLD', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP)
ON CONFLICT (ticket_id) DO UPDATE SET
    match_id = EXCLUDED.match_id,
    ticket_category_id = EXCLUDED.ticket_category_id,
    section_name = EXCLUDED.section_name,
    row_number = EXCLUDED.row_number,
    seat_number = EXCLUDED.seat_number,
    price = EXCLUDED.price,
    ticket_status = EXCLUDED.ticket_status,
    updated_at = EXCLUDED.updated_at;

-- ============================================================
-- 4. Features
-- ============================================================

INSERT INTO features
    (feature_id, feature_code, feature_name, description, created_at)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'PARKING', 'Parking', 'Parking access for the ticket holder.', CURRENT_TIMESTAMP),
    (2, 'CATERING', 'Catering', 'Food or snack service included.', CURRENT_TIMESTAMP),
    (3, 'COVERED_STAND', 'Covered Stand', 'Seat is located in a covered area.', CURRENT_TIMESTAMP),
    (4, 'DEDICATED_ENTRANCE', 'Dedicated Entrance', 'Separate entrance for faster access.', CURRENT_TIMESTAMP),
    (5, 'VIP_LOUNGE', 'VIP Lounge', 'Access to VIP lounge before the match.', CURRENT_TIMESTAMP),
    (6, 'NEAR_COURT', 'Near Court', 'Seat is close to the court.', CURRENT_TIMESTAMP),
    (7, 'FAMILY_ZONE', 'Family Zone', 'Family-friendly seating area.', CURRENT_TIMESTAMP),
    (8, 'ACCESSIBLE_SEAT', 'Accessible Seat', 'Suitable for users needing accessible seating.', CURRENT_TIMESTAMP),
    (9, 'MERCH_DISCOUNT', 'Merchandise Discount', 'Discount for official merchandise.', CURRENT_TIMESTAMP),
    (10, 'FAST_CHECKIN', 'Fast Check-in', 'Faster check-in at venue entrance.', CURRENT_TIMESTAMP)
ON CONFLICT (feature_id) DO UPDATE SET
    feature_code = EXCLUDED.feature_code,
    feature_name = EXCLUDED.feature_name,
    description = EXCLUDED.description;

-- ============================================================
-- 5. Ticket Features
-- ============================================================

INSERT INTO ticket_features
    (ticket_id, feature_id, created_at)
VALUES
    (1, 3, CURRENT_TIMESTAMP),
    (2, 1, CURRENT_TIMESTAMP),
    (2, 2, CURRENT_TIMESTAMP),
    (2, 4, CURRENT_TIMESTAMP),
    (2, 5, CURRENT_TIMESTAMP),
    (3, 3, CURRENT_TIMESTAMP),
    (3, 10, CURRENT_TIMESTAMP),
    (5, 1, CURRENT_TIMESTAMP),
    (5, 5, CURRENT_TIMESTAMP),
    (7, 9, CURRENT_TIMESTAMP),
    (10, 2, CURRENT_TIMESTAMP),
    (10, 4, CURRENT_TIMESTAMP),
    (10, 6, CURRENT_TIMESTAMP),
    (11, 6, CURRENT_TIMESTAMP),
    (13, 7, CURRENT_TIMESTAMP),
    (15, 6, CURRENT_TIMESTAMP),
    (16, 1, CURRENT_TIMESTAMP),
    (16, 2, CURRENT_TIMESTAMP),
    (16, 5, CURRENT_TIMESTAMP),
    (17, 6, CURRENT_TIMESTAMP),
    (20, 9, CURRENT_TIMESTAMP)
ON CONFLICT (ticket_id, feature_id) DO NOTHING;

-- ============================================================
-- 6. Football Details
-- ============================================================

INSERT INTO football_details
    (
        football_detail_id,
        ticket_id,
        league_name,
        stadium_name,
        gate_number,
        stand_number,
        row_number,
        seat_number,
        ticket_type,
        created_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    (1, 1, 'Persian Gulf Pro League', 'Azadi Stadium', 'E1', 'East Stand', 'A1', '001', 'NORMAL', CURRENT_TIMESTAMP),
    (2, 2, 'Persian Gulf Pro League', 'Azadi Stadium', 'VIP1', 'VIP West', 'V1', '010', 'VIP', CURRENT_TIMESTAMP),
    (3, 3, 'Persian Gulf Pro League', 'Azadi Stadium', 'C1', 'Covered Stand', 'C1', '021', 'SPECIAL', CURRENT_TIMESTAMP),
    (4, 4, 'Hazfi Cup', 'Azadi Stadium', 'E2', 'East Stand', 'A2', '045', 'NORMAL', CURRENT_TIMESTAMP),
    (5, 5, 'Hazfi Cup', 'Azadi Stadium', 'VIP2', 'VIP West', 'V2', '011', 'VIP', CURRENT_TIMESTAMP),
    (6, 6, 'Persian Gulf Pro League', 'Naghsh-e Jahan Stadium', 'N1', 'North Stand', 'N1', '101', 'NORMAL', CURRENT_TIMESTAMP),
    (7, 7, 'Persian Gulf Pro League', 'Naghsh-e Jahan Stadium', 'S1', 'Economy South', 'S1', '180', 'ECONOMY', CURRENT_TIMESTAMP),
    (8, 8, 'Persian Gulf Pro League', 'Yadegar-e Emam Stadium', 'M1', 'Main Stand', 'M1', '022', 'NORMAL', CURRENT_TIMESTAMP)
ON CONFLICT (football_detail_id) DO UPDATE SET
    ticket_id = EXCLUDED.ticket_id,
    league_name = EXCLUDED.league_name,
    stadium_name = EXCLUDED.stadium_name,
    gate_number = EXCLUDED.gate_number,
    stand_number = EXCLUDED.stand_number,
    row_number = EXCLUDED.row_number,
    seat_number = EXCLUDED.seat_number,
    ticket_type = EXCLUDED.ticket_type;

-- ============================================================
-- 7. Volleyball Details
-- ============================================================

INSERT INTO volleyball_details
    (
        volleyball_detail_id,
        ticket_id,
        league_name,
        hall_name,
        gate_number,
        stand_number,
        row_number,
        seat_number,
        ticket_type,
        created_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    (1, 9, 'National Volleyball League', 'Enghelab Sport Hall', 'A', 'Hall A', 'R1', '015', 'NORMAL', CURRENT_TIMESTAMP),
    (2, 10, 'National Volleyball League', 'Enghelab Sport Hall', 'VIP', 'VIP Court Side', 'V1', '002', 'VIP', CURRENT_TIMESTAMP),
    (3, 11, 'National Volleyball League', 'Enghelab Sport Hall', 'B', 'Near Court', 'R2', '018', 'SPECIAL', CURRENT_TIMESTAMP),
    (4, 12, 'National Volleyball League', 'Qom Volleyball Hall', 'C', 'Main Hall', 'R3', '030', 'NORMAL', CURRENT_TIMESTAMP),
    (5, 13, 'National Volleyball League', 'Qom Volleyball Hall', 'F', 'Family Area', 'F1', '008', 'FAMILY', CURRENT_TIMESTAMP),
    (6, 14, 'Volleyball Cup', 'Rasht Sport Hall', 'R', 'Rasht Hall', 'R4', '041', 'NORMAL', CURRENT_TIMESTAMP)
ON CONFLICT (volleyball_detail_id) DO UPDATE SET
    ticket_id = EXCLUDED.ticket_id,
    league_name = EXCLUDED.league_name,
    hall_name = EXCLUDED.hall_name,
    gate_number = EXCLUDED.gate_number,
    stand_number = EXCLUDED.stand_number,
    row_number = EXCLUDED.row_number,
    seat_number = EXCLUDED.seat_number,
    ticket_type = EXCLUDED.ticket_type;

-- ============================================================
-- 8. Basketball Details
-- ============================================================

INSERT INTO basketball_details
    (
        basketball_detail_id,
        ticket_id,
        league_name,
        hall_name,
        gate_number,
        stand_number,
        row_number,
        seat_number,
        ticket_type,
        created_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    (1, 15, 'National Basketball League', 'Tehran Basketball Arena', 'L', 'Arena Lower', 'L1', '006', 'NORMAL', CURRENT_TIMESTAMP),
    (2, 16, 'National Basketball League', 'Tehran Basketball Arena', 'VIP', 'VIP Court Side', 'V1', '001', 'VIP', CURRENT_TIMESTAMP),
    (3, 17, 'National Basketball League', 'Tehran Basketball Arena', 'P', 'Premium Lower', 'P1', '014', 'SPECIAL', CURRENT_TIMESTAMP),
    (4, 18, 'Basketball Cup', 'Ahvaz Basketball Arena', 'A', 'Ahvaz Arena', 'A1', '020', 'NORMAL', CURRENT_TIMESTAMP),
    (5, 19, 'Basketball Cup', 'Ahvaz Basketball Arena', 'VIP', 'VIP Arena', 'V2', '004', 'VIP', CURRENT_TIMESTAMP),
    (6, 20, 'National Basketball League', 'Karaj Football Stadium', 'K', 'Karaj Arena', 'K1', '033', 'NORMAL', CURRENT_TIMESTAMP)
ON CONFLICT (basketball_detail_id) DO UPDATE SET
    ticket_id = EXCLUDED.ticket_id,
    league_name = EXCLUDED.league_name,
    hall_name = EXCLUDED.hall_name,
    gate_number = EXCLUDED.gate_number,
    stand_number = EXCLUDED.stand_number,
    row_number = EXCLUDED.row_number,
    seat_number = EXCLUDED.seat_number,
    ticket_type = EXCLUDED.ticket_type;

-- ============================================================
-- 9. Cancellation Policies
-- ============================================================

INSERT INTO cancellation_policies
    (
        cancellation_policy_id,
        match_id,
        ticket_category_id,
        hours_before_match,
        penalty_percent,
        description,
        created_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    (1, 1, 1, 48, 10.00, 'Normal football ticket cancellation more than 48 hours before match.', CURRENT_TIMESTAMP),
    (2, 1, 2, 48, 20.00, 'VIP football ticket cancellation more than 48 hours before match.', CURRENT_TIMESTAMP),
    (3, 2, 1, 24, 15.00, 'Normal Hazfi Cup cancellation policy.', CURRENT_TIMESTAMP),
    (4, 5, 1, 24, 10.00, 'Normal volleyball ticket cancellation policy.', CURRENT_TIMESTAMP),
    (5, 5, 2, 24, 18.00, 'VIP volleyball ticket cancellation policy.', CURRENT_TIMESTAMP),
    (6, 8, 1, 24, 12.00, 'Normal basketball ticket cancellation policy.', CURRENT_TIMESTAMP),
    (7, 8, 2, 24, 22.00, 'VIP basketball ticket cancellation policy.', CURRENT_TIMESTAMP),
    (8, 9, 1, 12, 25.00, 'Basketball cup short-time cancellation policy.', CURRENT_TIMESTAMP),
    (9, 3, 4, 12, 30.00, 'Finished football event economy category cancellation policy.', CURRENT_TIMESTAMP),
    (10, 10, 1, 12, 20.00, 'Finished basketball event normal category cancellation policy.', CURRENT_TIMESTAMP)
ON CONFLICT (cancellation_policy_id) DO UPDATE SET
    match_id = EXCLUDED.match_id,
    ticket_category_id = EXCLUDED.ticket_category_id,
    hours_before_match = EXCLUDED.hours_before_match,
    penalty_percent = EXCLUDED.penalty_percent,
    description = EXCLUDED.description;
