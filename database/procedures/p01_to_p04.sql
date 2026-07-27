-- ============================================================
-- p01_to_p04.sql
-- Phase 2 - Stored Procedures / Functions 1 to 4
-- Project: Sports Match Ticket Reservation System
-- Owner: Sarina
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- 1. PostgreSQL functions are used instead of procedures because
--    they are easier to test with SELECT.
-- 2. Successful purchases are represented by:
--    reservations.reservation_status = 'PAID'
--    payments.payment_status = 'SUCCESS'
-- 3. Each function can be tested with a simple SELECT statement.

-- ============================================================
-- Procedure 1 / Function 1
-- با دریافت ایمیل یا شماره تلفن، لیست بلیط‌های خریداری‌شده توسط کاربر
-- را به ترتیب زمان خرید نمایش بده.
-- Get purchased tickets by user email or phone, ordered by purchase time.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_get_purchased_tickets_by_contact(
    p_contact TEXT
)
RETURNS TABLE (
    paid_at TIMESTAMP,
    user_id INTEGER,
    first_name VARCHAR,
    last_name VARCHAR,
    contact_info VARCHAR,
    ticket_id INTEGER,
    sport_name VARCHAR,
    match_title VARCHAR,
    venue_name VARCHAR,
    city_name VARCHAR,
    category_name VARCHAR,
    section_name VARCHAR,
    row_number VARCHAR,
    seat_number VARCHAR,
    price NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.paid_at::TIMESTAMP,
        u.user_id,
        u.first_name,
        u.last_name,
        COALESCE(u.email, u.phone_number)::VARCHAR AS contact_info,
        t.ticket_id,
        s.sport_name,
        m.match_title,
        v.venue_name,
        c.city_name,
        tc.category_name,
        t.section_name,
        t.row_number,
        t.seat_number,
        t.price
    FROM users u
    JOIN reservations r
        ON r.user_id = u.user_id
    JOIN payments p
        ON p.reservation_id = r.reservation_id
    JOIN tickets t
        ON t.ticket_id = r.ticket_id
    JOIN ticket_categories tc
        ON tc.ticket_category_id = t.ticket_category_id
    JOIN matches m
        ON m.match_id = t.match_id
    JOIN sports s
        ON s.sport_id = m.sport_id
    JOIN venues v
        ON v.venue_id = m.venue_id
    JOIN cities c
        ON c.city_id = v.city_id
    WHERE (u.email = p_contact OR u.phone_number = p_contact)
      AND r.reservation_status = 'PAID'
      AND p.payment_status = 'SUCCESS'
    ORDER BY
        p.paid_at ASC,
        t.ticket_id ASC;
END;
$$;

-- Test example:
-- SELECT * FROM fn_get_purchased_tickets_by_contact('ali.ahmadi@example.com');

-- ============================================================
-- Procedure 2 / Function 2
-- با دریافت ایمیل یا شماره تلفن پشتیبان، نام کاربرانی که حداقل یک بار
-- رزرو آنها لغو شده را لیست کن.
-- Get users whose reservations were cancelled by a specific support user.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_get_users_cancelled_by_support(
    p_support_contact TEXT
)
RETURNS TABLE (
    support_user_id INTEGER,
    support_first_name VARCHAR,
    support_last_name VARCHAR,
    customer_user_id INTEGER,
    customer_first_name VARCHAR,
    customer_last_name VARCHAR,
    customer_contact_info VARCHAR,
    cancelled_reservation_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        su.user_id AS support_user_id,
        su.first_name AS support_first_name,
        su.last_name AS support_last_name,
        cu.user_id AS customer_user_id,
        cu.first_name AS customer_first_name,
        cu.last_name AS customer_last_name,
        COALESCE(cu.email, cu.phone_number)::VARCHAR AS customer_contact_info,
        COUNT(DISTINCT r.reservation_id) AS cancelled_reservation_count
    FROM users su
    JOIN roles sr
        ON sr.role_id = su.role_id
    JOIN support_actions sa
        ON sa.support_user_id = su.user_id
    JOIN reservations r
        ON r.reservation_id = sa.reservation_id
    JOIN users cu
        ON cu.user_id = r.user_id
    WHERE (su.email = p_support_contact OR su.phone_number = p_support_contact)
      AND sr.role_code = 'SUPPORT'
      AND sa.action_type = 'CANCEL_RESERVATION'
      AND r.reservation_status = 'CANCELLED'
    GROUP BY
        su.user_id,
        su.first_name,
        su.last_name,
        cu.user_id,
        cu.first_name,
        cu.last_name,
        COALESCE(cu.email, cu.phone_number)
    ORDER BY
        cancelled_reservation_count DESC,
        cu.user_id ASC;
END;
$$;

-- Test example:
-- SELECT * FROM fn_get_users_cancelled_by_support('shayan.support@example.com');

-- ============================================================
-- Procedure 3 / Function 3
-- با دریافت نام شهر، لیست بلیط‌های خریداری‌شده در آن شهر را نمایش بده.
-- Get purchased tickets in a given venue city.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_get_purchased_tickets_by_city(
    p_city_name TEXT
)
RETURNS TABLE (
    paid_at TIMESTAMP,
    ticket_id INTEGER,
    user_id INTEGER,
    first_name VARCHAR,
    last_name VARCHAR,
    contact_info VARCHAR,
    sport_name VARCHAR,
    match_title VARCHAR,
    venue_name VARCHAR,
    city_name VARCHAR,
    category_name VARCHAR,
    section_name VARCHAR,
    row_number VARCHAR,
    seat_number VARCHAR,
    price NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.paid_at::TIMESTAMP,
        t.ticket_id,
        u.user_id,
        u.first_name,
        u.last_name,
        COALESCE(u.email, u.phone_number)::VARCHAR AS contact_info,
        s.sport_name,
        m.match_title,
        v.venue_name,
        c.city_name,
        tc.category_name,
        t.section_name,
        t.row_number,
        t.seat_number,
        t.price
    FROM cities c
    JOIN venues v
        ON v.city_id = c.city_id
    JOIN matches m
        ON m.venue_id = v.venue_id
    JOIN sports s
        ON s.sport_id = m.sport_id
    JOIN tickets t
        ON t.match_id = m.match_id
    JOIN ticket_categories tc
        ON tc.ticket_category_id = t.ticket_category_id
    JOIN reservations r
        ON r.ticket_id = t.ticket_id
    JOIN users u
        ON u.user_id = r.user_id
    JOIN payments p
        ON p.reservation_id = r.reservation_id
    WHERE LOWER(c.city_name) = LOWER(p_city_name)
      AND r.reservation_status = 'PAID'
      AND p.payment_status = 'SUCCESS'
    ORDER BY
        p.paid_at ASC,
        t.ticket_id ASC;
END;
$$;

-- Test example:
-- SELECT * FROM fn_get_purchased_tickets_by_city('Tehran');

-- ============================================================
-- Procedure 4 / Function 4
-- عبارتی را از ورودی گرفته و بلیط‌هایی را که آن عبارت در نام تماشاگر،
-- نام تیم‌ها، محل برگزاری یا رده بلیط آمده باشد برگردان.
-- Search purchased tickets by spectator name, team names, venue name,
-- or ticket category.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_search_purchased_tickets(
    p_search_text TEXT
)
RETURNS TABLE (
    paid_at TIMESTAMP,
    ticket_id INTEGER,
    user_id INTEGER,
    spectator_name TEXT,
    contact_info VARCHAR,
    sport_name VARCHAR,
    match_title VARCHAR,
    home_team_name VARCHAR,
    away_team_name VARCHAR,
    venue_name VARCHAR,
    city_name VARCHAR,
    category_name VARCHAR,
    section_name VARCHAR,
    row_number VARCHAR,
    seat_number VARCHAR,
    price NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.paid_at::TIMESTAMP,
        t.ticket_id,
        u.user_id,
        (u.first_name || ' ' || u.last_name) AS spectator_name,
        COALESCE(u.email, u.phone_number)::VARCHAR AS contact_info,
        s.sport_name,
        m.match_title,
        ht.team_name AS home_team_name,
        at.team_name AS away_team_name,
        v.venue_name,
        c.city_name,
        tc.category_name,
        t.section_name,
        t.row_number,
        t.seat_number,
        t.price
    FROM reservations r
    JOIN payments p
        ON p.reservation_id = r.reservation_id
    JOIN users u
        ON u.user_id = r.user_id
    JOIN tickets t
        ON t.ticket_id = r.ticket_id
    JOIN ticket_categories tc
        ON tc.ticket_category_id = t.ticket_category_id
    JOIN matches m
        ON m.match_id = t.match_id
    JOIN sports s
        ON s.sport_id = m.sport_id
    JOIN teams ht
        ON ht.team_id = m.home_team_id
    JOIN teams at
        ON at.team_id = m.away_team_id
    JOIN venues v
        ON v.venue_id = m.venue_id
    JOIN cities c
        ON c.city_id = v.city_id
    WHERE r.reservation_status = 'PAID'
      AND p.payment_status = 'SUCCESS'
      AND (
            LOWER(u.first_name || ' ' || u.last_name) LIKE '%' || LOWER(p_search_text) || '%'
         OR LOWER(ht.team_name) LIKE '%' || LOWER(p_search_text) || '%'
         OR LOWER(at.team_name) LIKE '%' || LOWER(p_search_text) || '%'
         OR LOWER(v.venue_name) LIKE '%' || LOWER(p_search_text) || '%'
         OR LOWER(tc.category_name) LIKE '%' || LOWER(p_search_text) || '%'
         OR LOWER(tc.category_code) LIKE '%' || LOWER(p_search_text) || '%'
      )
    ORDER BY
        p.paid_at ASC,
        t.ticket_id ASC;
END;
$$;

-- Test examples:
-- SELECT * FROM fn_search_purchased_tickets('Ali');
-- SELECT * FROM fn_search_purchased_tickets('Azadi');
-- SELECT * FROM fn_search_purchased_tickets('VIP');
