-- ============================================================
-- q08_to_q15.sql
-- Phase 2 - Analytical Queries 8 to 15
-- Project: Sports Match Ticket Reservation System
-- Owner: Sarina
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- 1. These queries assume that successful purchases are represented by:
--    reservations.reservation_status = 'PAID'
--    payments.payment_status = 'SUCCESS'
-- 2. Payment time is based on payments.paid_at.
-- 3. Venue city is considered the city of the purchased ticket.
-- 4. Query 13 uses Football as the selected sport type.
--    Change the value inside the selected_sport CTE if another sport is needed.

-- ============================================================
-- Query 8
-- نام 3 کاربر با بیشترین خرید بلیط در هفته اخیر.
-- Top 3 users with the highest number of successful ticket purchases
-- during the last 7 days.
-- ============================================================

SELECT
    u.user_id,
    u.first_name,
    u.last_name,
    COALESCE(u.email, u.phone_number) AS contact_info,
    COUNT(DISTINCT r.reservation_id) AS purchased_ticket_count
FROM users u
JOIN reservations r
    ON r.user_id = u.user_id
JOIN payments p
    ON p.reservation_id = r.reservation_id
WHERE r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
  AND p.paid_at >= CURRENT_TIMESTAMP - INTERVAL '7 days'
GROUP BY
    u.user_id,
    u.first_name,
    u.last_name,
    COALESCE(u.email, u.phone_number)
ORDER BY
    purchased_ticket_count DESC,
    u.user_id ASC
LIMIT 3;

-- ============================================================
-- Query 9
-- تعداد بلیط‌های فروخته‌شده در استان تهران به تفکیک شهر.
-- Count sold tickets in Tehran province grouped by city.
-- ============================================================

SELECT
    c.city_id,
    c.city_name,
    c.province_name,
    COUNT(DISTINCT t.ticket_id) AS sold_ticket_count
FROM cities c
JOIN venues v
    ON v.city_id = c.city_id
JOIN matches m
    ON m.venue_id = v.venue_id
JOIN tickets t
    ON t.match_id = m.match_id
JOIN reservations r
    ON r.ticket_id = t.ticket_id
JOIN payments p
    ON p.reservation_id = r.reservation_id
WHERE c.province_name = 'Tehran'
  AND r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
GROUP BY
    c.city_id,
    c.city_name,
    c.province_name
ORDER BY
    sold_ticket_count DESC,
    c.city_name ASC;

-- ============================================================
-- Query 10
-- نام شهرهایی که قدیمی‌ترین کاربر ثبت‌نام‌شده از آنجا خرید داشته است.
-- Cities where the oldest registered user has purchased tickets.
-- ============================================================

WITH oldest_user AS (
    SELECT
        u.user_id,
        u.first_name,
        u.last_name,
        u.registered_at
    FROM users u
    ORDER BY
        u.registered_at ASC,
        u.user_id ASC
    LIMIT 1
)
SELECT DISTINCT
    c.city_id,
    c.city_name,
    c.province_name,
    ou.user_id,
    ou.first_name,
    ou.last_name,
    ou.registered_at
FROM oldest_user ou
JOIN reservations r
    ON r.user_id = ou.user_id
JOIN payments p
    ON p.reservation_id = r.reservation_id
JOIN tickets t
    ON t.ticket_id = r.ticket_id
JOIN matches m
    ON m.match_id = t.match_id
JOIN venues v
    ON v.venue_id = m.venue_id
JOIN cities c
    ON c.city_id = v.city_id
WHERE r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
ORDER BY
    c.city_name ASC;

-- ============================================================
-- Query 11
-- نام پشتیبان‌های سایت.
-- List website support users.
-- ============================================================

SELECT
    u.user_id,
    u.first_name,
    u.last_name,
    COALESCE(u.email, u.phone_number) AS contact_info,
    u.account_status
FROM users u
JOIN roles r
    ON r.role_id = u.role_id
WHERE r.role_code = 'SUPPORT'
ORDER BY
    u.user_id ASC;

-- ============================================================
-- Query 12
-- نام کاربرانی که حداقل 2 بلیط در سیستم خریداری کرده‌اند.
-- Users who have purchased at least 2 tickets.
-- ============================================================

SELECT
    u.user_id,
    u.first_name,
    u.last_name,
    COALESCE(u.email, u.phone_number) AS contact_info,
    COUNT(DISTINCT r.reservation_id) AS purchased_ticket_count
FROM users u
JOIN reservations r
    ON r.user_id = u.user_id
JOIN payments p
    ON p.reservation_id = r.reservation_id
WHERE r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
GROUP BY
    u.user_id,
    u.first_name,
    u.last_name,
    COALESCE(u.email, u.phone_number)
HAVING COUNT(DISTINCT r.reservation_id) >= 2
ORDER BY
    purchased_ticket_count DESC,
    u.user_id ASC;

-- ============================================================
-- Query 13
-- نام کاربرانی که حداکثر 2 بلیط از یک نوع مسابقه خاص مثل فوتبال خریده‌اند.
-- Users who bought at most 2 tickets from a selected sport type.
-- Default selected sport: Football.
-- ============================================================

WITH selected_sport AS (
    SELECT 'Football'::VARCHAR AS sport_name
),
sport_purchase_counts AS (
    SELECT
        u.user_id,
        u.first_name,
        u.last_name,
        COALESCE(u.email, u.phone_number) AS contact_info,
        COUNT(DISTINCT r.reservation_id) AS purchased_ticket_count
    FROM users u
    JOIN reservations r
        ON r.user_id = u.user_id
    JOIN payments p
        ON p.reservation_id = r.reservation_id
    JOIN tickets t
        ON t.ticket_id = r.ticket_id
    JOIN matches m
        ON m.match_id = t.match_id
    JOIN sports s
        ON s.sport_id = m.sport_id
    CROSS JOIN selected_sport ss
    WHERE r.reservation_status = 'PAID'
      AND p.payment_status = 'SUCCESS'
      AND LOWER(s.sport_name) = LOWER(ss.sport_name)
    GROUP BY
        u.user_id,
        u.first_name,
        u.last_name,
        COALESCE(u.email, u.phone_number)
)
SELECT
    user_id,
    first_name,
    last_name,
    contact_info,
    purchased_ticket_count
FROM sport_purchase_counts
WHERE purchased_ticket_count <= 2
ORDER BY
    purchased_ticket_count DESC,
    user_id ASC;

-- ============================================================
-- Query 14
-- ایمیل یا شماره تلفن کاربرانی که از تمام انواع مسابقات فوتبال،
-- والیبال و بسکتبال حداقل یک بار بلیط خریده‌اند.
-- Contact information of users who bought at least one ticket from
-- all three main sports: Football, Volleyball, Basketball.
-- ============================================================

SELECT
    u.user_id,
    u.first_name,
    u.last_name,
    COALESCE(u.email, u.phone_number) AS contact_info,
    COUNT(DISTINCT LOWER(s.sport_name)) AS purchased_main_sport_count
FROM users u
JOIN reservations r
    ON r.user_id = u.user_id
JOIN payments p
    ON p.reservation_id = r.reservation_id
JOIN tickets t
    ON t.ticket_id = r.ticket_id
JOIN matches m
    ON m.match_id = t.match_id
JOIN sports s
    ON s.sport_id = m.sport_id
WHERE r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
  AND LOWER(s.sport_name) IN ('football', 'volleyball', 'basketball')
GROUP BY
    u.user_id,
    u.first_name,
    u.last_name,
    COALESCE(u.email, u.phone_number)
HAVING COUNT(DISTINCT LOWER(s.sport_name)) = 3
ORDER BY
    u.user_id ASC;

-- ============================================================
-- Query 15
-- اطلاعات بلیط‌های خریداری‌شده امروز با ترتیب ساعت خرید.
-- Purchased tickets today ordered by purchase time.
-- ============================================================

SELECT
    p.paid_at,
    u.user_id,
    u.first_name,
    u.last_name,
    COALESCE(u.email, u.phone_number) AS contact_info,
    t.ticket_id,
    t.section_name,
    t.row_number,
    t.seat_number,
    t.price,
    tc.category_name,
    s.sport_name,
    m.match_title,
    v.venue_name,
    c.city_name
FROM payments p
JOIN reservations r
    ON r.reservation_id = p.reservation_id
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
JOIN venues v
    ON v.venue_id = m.venue_id
JOIN cities c
    ON c.city_id = v.city_id
WHERE r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
  AND p.paid_at::DATE = CURRENT_DATE
ORDER BY
    p.paid_at ASC,
    t.ticket_id ASC;
