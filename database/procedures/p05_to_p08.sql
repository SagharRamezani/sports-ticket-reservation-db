-- ============================================================
-- p05_to_p08.sql
-- Phase 2 - Stored Procedures / Functions 5 to 8
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
-- 3. Cancelled tickets are represented by:
--    reservations.reservation_status = 'CANCELLED'
--    or tickets.ticket_status = 'CANCELLED'
-- 4. Each function can be tested with a simple SELECT statement.

-- ============================================================
-- Procedure 5 / Function 5
-- شماره تلفن یا ایمیل کاربر را دریافت کرده و اطلاعات سایر کاربران
-- همشهری او را نمایش بده.
-- Get other users from the same city as the given user.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_get_users_from_same_city(
    p_contact TEXT
)
RETURNS TABLE (
    base_user_id INTEGER,
    base_first_name VARCHAR,
    base_last_name VARCHAR,
    city_id INTEGER,
    city_name VARCHAR,
    province_name VARCHAR,
    other_user_id INTEGER,
    other_first_name VARCHAR,
    other_last_name VARCHAR,
    other_contact_info VARCHAR,
    other_account_status VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    WITH base_user AS (
        SELECT
            u.user_id,
            u.first_name,
            u.last_name,
            u.city_id
        FROM users u
        WHERE u.email = p_contact
           OR u.phone_number = p_contact
        LIMIT 1
    )
    SELECT
        bu.user_id AS base_user_id,
        bu.first_name AS base_first_name,
        bu.last_name AS base_last_name,
        c.city_id,
        c.city_name,
        c.province_name,
        ou.user_id AS other_user_id,
        ou.first_name AS other_first_name,
        ou.last_name AS other_last_name,
        COALESCE(ou.email, ou.phone_number) AS other_contact_info,
        ou.account_status AS other_account_status
    FROM base_user bu
    JOIN cities c
        ON c.city_id = bu.city_id
    JOIN users ou
        ON ou.city_id = bu.city_id
    WHERE ou.user_id <> bu.user_id
    ORDER BY
        ou.user_id ASC;
END;
$$;

-- Test examples:
-- SELECT * FROM fn_get_users_from_same_city('ali.ahmadi@example.com');
-- SELECT * FROM fn_get_users_from_same_city('09120000001');

-- ============================================================
-- Procedure 6 / Function 6
-- تاریخ و تعداد n را دریافت کرده و لیست n کاربری که از آن تاریخ به بعد
-- بیشترین خرید بلیط را داشته‌اند نمایش بده.
-- Get top N buyers after a selected date.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_get_top_buyers_after_date(
    p_from_date DATE,
    p_limit INTEGER
)
RETURNS TABLE (
    user_id INTEGER,
    first_name VARCHAR,
    last_name VARCHAR,
    contact_info VARCHAR,
    purchased_ticket_count BIGINT,
    total_paid_amount NUMERIC,
    first_purchase_at TIMESTAMP,
    last_purchase_at TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        u.user_id,
        u.first_name,
        u.last_name,
        COALESCE(u.email, u.phone_number) AS contact_info,
        COUNT(DISTINCT r.reservation_id) AS purchased_ticket_count,
        SUM(p.amount) AS total_paid_amount,
        MIN(p.paid_at) AS first_purchase_at,
        MAX(p.paid_at) AS last_purchase_at
    FROM users u
    JOIN reservations r
        ON r.user_id = u.user_id
    JOIN payments p
        ON p.reservation_id = r.reservation_id
    WHERE r.reservation_status = 'PAID'
      AND p.payment_status = 'SUCCESS'
      AND p.paid_at::DATE >= p_from_date
    GROUP BY
        u.user_id,
        u.first_name,
        u.last_name,
        COALESCE(u.email, u.phone_number)
    ORDER BY
        purchased_ticket_count DESC,
        total_paid_amount DESC,
        u.user_id ASC
    LIMIT p_limit;
END;
$$;

-- Test example:
-- SELECT * FROM fn_get_top_buyers_after_date(CURRENT_DATE - INTERVAL '7 days', 3);

-- ============================================================
-- Procedure 7 / Function 7
-- با دریافت نوع مسابقه ورزشی، لیست بلیط‌های کنسل‌شده مربوط به آن را
-- به ترتیب تاریخ نمایش بده.
-- Get cancelled tickets by sport type ordered by cancellation date.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_get_cancelled_tickets_by_sport(
    p_sport_name TEXT
)
RETURNS TABLE (
    cancelled_at TIMESTAMP,
    reservation_id INTEGER,
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
    price NUMERIC,
    cancellation_reason TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        r.cancelled_at,
        r.reservation_id,
        t.ticket_id,
        u.user_id,
        u.first_name,
        u.last_name,
        COALESCE(u.email, u.phone_number) AS contact_info,
        s.sport_name,
        m.match_title,
        v.venue_name,
        c.city_name,
        tc.category_name,
        t.section_name,
        t.row_number,
        t.seat_number,
        t.price,
        r.cancellation_reason
    FROM sports s
    JOIN matches m
        ON m.sport_id = s.sport_id
    JOIN venues v
        ON v.venue_id = m.venue_id
    JOIN cities c
        ON c.city_id = v.city_id
    JOIN tickets t
        ON t.match_id = m.match_id
    JOIN ticket_categories tc
        ON tc.ticket_category_id = t.ticket_category_id
    JOIN reservations r
        ON r.ticket_id = t.ticket_id
    JOIN users u
        ON u.user_id = r.user_id
    WHERE LOWER(s.sport_name) = LOWER(p_sport_name)
      AND (
            r.reservation_status = 'CANCELLED'
            OR t.ticket_status = 'CANCELLED'
      )
    ORDER BY
        r.cancelled_at ASC NULLS LAST,
        r.reservation_id ASC,
        t.ticket_id ASC;
END;
$$;

-- Test examples:
-- SELECT * FROM fn_get_cancelled_tickets_by_sport('Football');
-- SELECT * FROM fn_get_cancelled_tickets_by_sport('Basketball');

-- ============================================================
-- Procedure 8 / Function 8
-- با دریافت موضوع گزارش، لیست کاربرانی که بیشترین گزارش در آن موضوع دارند
-- را نمایش بده.
-- Get users with the highest number of reports in a selected report category.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_get_top_reporters_by_category(
    p_report_category TEXT
)
RETURNS TABLE (
    report_category_id INTEGER,
    category_code VARCHAR,
    category_name VARCHAR,
    reporter_user_id INTEGER,
    first_name VARCHAR,
    last_name VARCHAR,
    contact_info VARCHAR,
    report_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    WITH reporter_counts AS (
        SELECT
            rc.report_category_id,
            rc.category_code,
            rc.category_name,
            u.user_id AS reporter_user_id,
            u.first_name,
            u.last_name,
            COALESCE(u.email, u.phone_number) AS contact_info,
            COUNT(r.report_id) AS report_count,
            DENSE_RANK() OVER (
                PARTITION BY rc.report_category_id
                ORDER BY COUNT(r.report_id) DESC
            ) AS report_rank
        FROM report_categories rc
        JOIN reports r
            ON r.report_category_id = rc.report_category_id
        JOIN users u
            ON u.user_id = r.reporter_user_id
        WHERE LOWER(rc.category_code) = LOWER(p_report_category)
           OR LOWER(rc.category_name) = LOWER(p_report_category)
        GROUP BY
            rc.report_category_id,
            rc.category_code,
            rc.category_name,
            u.user_id,
            u.first_name,
            u.last_name,
            COALESCE(u.email, u.phone_number)
    )
    SELECT
        reporter_counts.report_category_id,
        reporter_counts.category_code,
        reporter_counts.category_name,
        reporter_counts.reporter_user_id,
        reporter_counts.first_name,
        reporter_counts.last_name,
        reporter_counts.contact_info,
        reporter_counts.report_count
    FROM reporter_counts
    WHERE reporter_counts.report_rank = 1
    ORDER BY
        reporter_counts.report_count DESC,
        reporter_counts.reporter_user_id ASC;
END;
$$;

-- Test examples:
-- SELECT * FROM fn_get_top_reporters_by_category('Payment Issue');
-- SELECT * FROM fn_get_top_reporters_by_category('SEAT_ISSUE');
