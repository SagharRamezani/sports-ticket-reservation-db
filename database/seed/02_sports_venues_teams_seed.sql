-- ============================================================
-- 02_sports_venues_teams_seed.sql
-- Phase 2 - Seed data for sports, venues, and teams
-- Project: Sports Match Ticket Reservation System
-- Owner: Shamim
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- 1. This file depends on:
--    database/seed/01_users_seed.sql
--    because venues and teams reference city IDs from cities.
-- 2. Explicit IDs are used so later seed files can reference stable FK values.
-- 3. OVERRIDING SYSTEM VALUE is required because the schema uses
--    GENERATED ALWAYS AS IDENTITY.
-- 4. This file includes:
--    - Football, Volleyball, Basketball, and other sport types
--    - Venues in Tehran and other cities
--    - Azadi Stadium for Tehran-related analytical queries
--    - Teams for football, volleyball, and basketball matches

-- ============================================================
-- 1. Sports
-- ============================================================

INSERT INTO sports
    (sport_id, sport_code, sport_name, description, created_at)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'FOOTBALL', 'Football', 'Football matches and league games.', CURRENT_TIMESTAMP),
    (2, 'VOLLEYBALL', 'Volleyball', 'Volleyball league and cup matches.', CURRENT_TIMESTAMP),
    (3, 'BASKETBALL', 'Basketball', 'Basketball league and tournament matches.', CURRENT_TIMESTAMP),
    (4, 'FUTSAL', 'Futsal', 'Indoor futsal matches.', CURRENT_TIMESTAMP),
    (5, 'HANDBALL', 'Handball', 'Handball matches and events.', CURRENT_TIMESTAMP)
ON CONFLICT (sport_id) DO UPDATE SET
    sport_code = EXCLUDED.sport_code,
    sport_name = EXCLUDED.sport_name,
    description = EXCLUDED.description;

-- ============================================================
-- 2. Venues
-- ============================================================

INSERT INTO venues
    (venue_id, city_id, venue_name, address, total_capacity, venue_type, created_at)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 1, 'Azadi Stadium', 'Azadi Sport Complex, Tehran', 78000, 'STADIUM', CURRENT_TIMESTAMP),
    (2, 1, 'Enghelab Sport Hall', 'Enghelab Sport Complex, Tehran', 3500, 'HALL', CURRENT_TIMESTAMP),
    (3, 2, 'Rey Sport Hall', 'Main Sport Boulevard, Rey', 2500, 'HALL', CURRENT_TIMESTAMP),
    (4, 3, 'Karaj Football Stadium', 'Azadi Street, Karaj', 12000, 'STADIUM', CURRENT_TIMESTAMP),
    (5, 4, 'Naghsh-e Jahan Stadium', 'Naghsh-e Jahan Complex, Isfahan', 75000, 'STADIUM', CURRENT_TIMESTAMP),
    (6, 5, 'Shiraz Shahid Dastgheib Stadium', 'Dastgheib Street, Shiraz', 20000, 'STADIUM', CURRENT_TIMESTAMP),
    (7, 6, 'Imam Reza Stadium', 'Imam Reza Boulevard, Mashhad', 27000, 'STADIUM', CURRENT_TIMESTAMP),
    (8, 7, 'Yadegar-e Emam Stadium', 'Elgoli Road, Tabriz', 66000, 'STADIUM', CURRENT_TIMESTAMP),
    (9, 8, 'Rasht Sport Hall', 'Golsar District, Rasht', 3000, 'HALL', CURRENT_TIMESTAMP),
    (10, 9, 'Ahvaz Basketball Arena', 'Kianpars, Ahvaz', 4200, 'ARENA', CURRENT_TIMESTAMP),
    (11, 10, 'Qom Volleyball Hall', 'Amin Boulevard, Qom', 2800, 'HALL', CURRENT_TIMESTAMP),
    (12, 1, 'Tehran Basketball Arena', 'Hemmat Highway, Tehran', 6000, 'ARENA', CURRENT_TIMESTAMP)
ON CONFLICT (venue_id) DO UPDATE SET
    city_id = EXCLUDED.city_id,
    venue_name = EXCLUDED.venue_name,
    address = EXCLUDED.address,
    total_capacity = EXCLUDED.total_capacity,
    venue_type = EXCLUDED.venue_type;

-- ============================================================
-- 3. Teams
-- ============================================================

INSERT INTO teams
    (team_id, sport_id, city_id, team_name, short_name, created_at)
OVERRIDING SYSTEM VALUE
VALUES
    -- Football teams
    (1, 1, 1, 'Tehran Reds FC', 'TRFC', CURRENT_TIMESTAMP),
    (2, 1, 1, 'Tehran Blues FC', 'TBFC', CURRENT_TIMESTAMP),
    (3, 1, 4, 'Isfahan Lions FC', 'ILFC', CURRENT_TIMESTAMP),
    (4, 1, 7, 'Tabriz Eagles FC', 'TEFC', CURRENT_TIMESTAMP),

    -- Volleyball teams
    (5, 2, 1, 'Tehran Spikers VC', 'TSVC', CURRENT_TIMESTAMP),
    (6, 2, 10, 'Qom Setters VC', 'QSVC', CURRENT_TIMESTAMP),
    (7, 2, 6, 'Mashhad Net Stars VC', 'MNSV', CURRENT_TIMESTAMP),
    (8, 2, 8, 'Rasht Waves VC', 'RWVC', CURRENT_TIMESTAMP),

    -- Basketball teams
    (9, 3, 1, 'Tehran Hoopers BC', 'THBC', CURRENT_TIMESTAMP),
    (10, 3, 9, 'Ahvaz Dunkers BC', 'ADBC', CURRENT_TIMESTAMP),
    (11, 3, 5, 'Shiraz Shooters BC', 'SSBC', CURRENT_TIMESTAMP),
    (12, 3, 3, 'Karaj Falcons BC', 'KFBC', CURRENT_TIMESTAMP),

    -- Other sport teams for optional diversity
    (13, 4, 2, 'Rey Futsal Club', 'RFC', CURRENT_TIMESTAMP),
    (14, 5, 4, 'Isfahan Handball Club', 'IHC', CURRENT_TIMESTAMP)
ON CONFLICT (team_id) DO UPDATE SET
    sport_id = EXCLUDED.sport_id,
    city_id = EXCLUDED.city_id,
    team_name = EXCLUDED.team_name,
    short_name = EXCLUDED.short_name;
