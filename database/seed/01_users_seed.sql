-- ============================================================
-- 01_users_seed.sql
-- Phase 2 - Seed data for roles, cities, and users
-- Project: Sports Match Ticket Reservation System
-- Owner: Shamim
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- 1. Explicit IDs are used so later seed files can reference stable FK values.
-- 2. OVERRIDING SYSTEM VALUE is required because the schema uses
--    GENERATED ALWAYS AS IDENTITY.
-- 3. This file includes:
--    - CUSTOMER and SUPPORT roles
--    - 10 cities
--    - 12 users
--    - at least 2 support users
--    - at least 1 old registered user
--    - at least 1 normal user intended to have no reservation in later seeds

-- ============================================================
-- 1. Roles
-- ============================================================

INSERT INTO roles
    (role_id, role_code, role_name, description, created_at)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'CUSTOMER', 'Customer', 'Normal user who can search, reserve, and buy sport match tickets.', CURRENT_TIMESTAMP),
    (2, 'SUPPORT', 'Support User', 'Support user who can review reports and problematic reservations.', CURRENT_TIMESTAMP)
ON CONFLICT (role_id) DO UPDATE SET
    role_code = EXCLUDED.role_code,
    role_name = EXCLUDED.role_name,
    description = EXCLUDED.description;

-- ============================================================
-- 2. Cities
-- ============================================================

INSERT INTO cities
    (city_id, city_name, province_name, created_at)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Tehran', 'Tehran', CURRENT_TIMESTAMP),
    (2, 'Rey', 'Tehran', CURRENT_TIMESTAMP),
    (3, 'Karaj', 'Alborz', CURRENT_TIMESTAMP),
    (4, 'Isfahan', 'Isfahan', CURRENT_TIMESTAMP),
    (5, 'Shiraz', 'Fars', CURRENT_TIMESTAMP),
    (6, 'Mashhad', 'Khorasan Razavi', CURRENT_TIMESTAMP),
    (7, 'Tabriz', 'East Azerbaijan', CURRENT_TIMESTAMP),
    (8, 'Rasht', 'Gilan', CURRENT_TIMESTAMP),
    (9, 'Ahvaz', 'Khuzestan', CURRENT_TIMESTAMP),
    (10, 'Qom', 'Qom', CURRENT_TIMESTAMP)
ON CONFLICT (city_id) DO UPDATE SET
    city_name = EXCLUDED.city_name,
    province_name = EXCLUDED.province_name;

-- ============================================================
-- 3. Users
-- ============================================================

INSERT INTO users
    (
        user_id,
        role_id,
        city_id,
        first_name,
        last_name,
        email,
        phone_number,
        password_hash,
        profile_image_url,
        account_status,
        registered_at,
        updated_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    -- Old registered customer. Later seeds should give this user successful purchases.
    (1, 1, 1, 'Ali', 'Ahmadi', 'ali.ahmadi@example.com', '09120000001',
     'hashed_password_ali', NULL, 'ACTIVE', TIMESTAMP '2023-01-10 09:00:00', NULL),

    -- Customer with several successful purchases in later seeds.
    (2, 1, 1, 'Sara', 'Mohammadi', 'sara.mohammadi@example.com', '09120000002',
     'hashed_password_sara', NULL, 'ACTIVE', TIMESTAMP '2024-02-15 10:30:00', NULL),

    (3, 1, 2, 'Reza', 'Karimi', 'reza.karimi@example.com', '09120000003',
     'hashed_password_reza', NULL, 'ACTIVE', TIMESTAMP '2024-03-20 11:15:00', NULL),

    (4, 1, 3, 'Niloofar', 'Hosseini', 'niloofar.hosseini@example.com', '09120000004',
     'hashed_password_niloofar', NULL, 'ACTIVE', TIMESTAMP '2024-04-05 14:20:00', NULL),

    (5, 1, 4, 'Amir', 'Moradi', 'amir.moradi@example.com', '09120000005',
     'hashed_password_amir', NULL, 'ACTIVE', TIMESTAMP '2024-05-12 08:45:00', NULL),

    (6, 1, 5, 'Mahsa', 'Ebrahimi', 'mahsa.ebrahimi@example.com', '09120000006',
     'hashed_password_mahsa', NULL, 'ACTIVE', TIMESTAMP '2024-06-01 16:10:00', NULL),

    (7, 1, 6, 'Hossein', 'Rahimi', 'hossein.rahimi@example.com', '09120000007',
     'hashed_password_hossein', NULL, 'ACTIVE', TIMESTAMP '2024-07-18 12:00:00', NULL),

    (8, 1, 7, 'Mina', 'Jafari', 'mina.jafari@example.com', '09120000008',
     'hashed_password_mina', NULL, 'ACTIVE', TIMESTAMP '2024-08-25 18:35:00', NULL),

    (9, 1, 8, 'Parsa', 'Sadeghi', 'parsa.sadeghi@example.com', '09120000009',
     'hashed_password_parsa', NULL, 'INACTIVE', TIMESTAMP '2024-09-09 13:25:00', NULL),

    -- This customer is intentionally kept without any reservation in later seed files
    -- so Query 1 can return a meaningful result.
    (10, 1, 9, 'Leila', 'Rostami', 'leila.rostami@example.com', '09120000010',
     'hashed_password_leila', NULL, 'ACTIVE', TIMESTAMP '2024-10-14 15:40:00', NULL),

    -- Support users.
    (11, 2, 1, 'Shayan', 'Supporti', 'shayan.support@example.com', '09120000011',
     'hashed_password_shayan', NULL, 'ACTIVE', TIMESTAMP '2024-01-05 09:30:00', NULL),

    (12, 2, 4, 'Negar', 'Admini', 'negar.admin@example.com', '09120000012',
     'hashed_password_negar', NULL, 'ACTIVE', TIMESTAMP '2024-01-07 10:45:00', NULL)
ON CONFLICT (user_id) DO UPDATE SET
    role_id = EXCLUDED.role_id,
    city_id = EXCLUDED.city_id,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    email = EXCLUDED.email,
    phone_number = EXCLUDED.phone_number,
    password_hash = EXCLUDED.password_hash,
    profile_image_url = EXCLUDED.profile_image_url,
    account_status = EXCLUDED.account_status,
    registered_at = EXCLUDED.registered_at,
    updated_at = EXCLUDED.updated_at;
