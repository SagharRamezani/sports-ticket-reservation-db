-- ============================================================
-- q01_to_q07.sql
-- Phase 2 - Analytical Queries 1 to 7
-- Project: Sports Match Ticket Reservation System
-- Owner: Saghar
-- Database: PostgreSQL
-- ============================================================

-- ============================================================
-- Query 1
-- نام و نام خانوادگی کاربرانی که تا به حال هیچ بلیطی رزرو نکرده‌اند.
-- Users who have never reserved any ticket.
-- ============================================================

SELECT u.user_id,
       u.first_name,
       u.last_name
FROM users u
WHERE NOT EXISTS (SELECT 1
                  FROM reservations r
                  WHERE r.user_id = u.user_id)
ORDER BY u.user_id;


-- ============================================================
-- Query 2
-- نام و نام خانوادگی تمام کاربرانی که حداقل یک بلیط خریده‌اند.
-- Users who have bought at least one ticket.
--
-- Definition of bought ticket:
-- A reservation is considered purchased when:
-- reservation_status = 'PAID'
-- and it has at least one successful payment.
-- ============================================================

SELECT DISTINCT u.user_id,
                u.first_name,
                u.last_name
FROM users u
         JOIN reservations r
              ON r.user_id = u.user_id
         JOIN payments p
              ON p.reservation_id = r.reservation_id
WHERE r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
ORDER BY u.user_id;


-- ============================================================
-- Query 3
-- مجموع پرداخت‌های انجام‌شده توسط هر کاربر در ماه‌های مختلف.
-- Total successful payments of each user by month.
-- ============================================================

SELECT u.user_id,
       u.first_name,
       u.last_name,
       DATE_TRUNC('month', p.paid_at)::date AS payment_month, SUM(p.amount) AS total_paid
FROM users u
         JOIN payments p
              ON p.user_id = u.user_id
WHERE p.payment_status = 'SUCCESS'
  AND p.paid_at IS NOT NULL
GROUP BY u.user_id,
         u.first_name,
         u.last_name,
         DATE_TRUNC('month', p.paid_at)
ORDER BY payment_month,
         u.user_id;


-- ============================================================
-- Query 4
-- لیست کاربرانی که در هر شهر فقط یک بار بلیط خریداری کرده‌اند.
-- Users who have bought exactly one ticket in each venue city.
--
-- Definition:
-- The city is considered the city of the venue where the match is held.
-- A bought ticket requires reservation_status = 'PAID'
-- and payment_status = 'SUCCESS'.
-- ============================================================

SELECT c.city_id,
       c.city_name,
       c.province_name,
       u.user_id,
       u.first_name,
       u.last_name,
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
         JOIN venues v
              ON v.venue_id = m.venue_id
         JOIN cities c
              ON c.city_id = v.city_id
WHERE r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
GROUP BY c.city_id,
         c.city_name,
         c.province_name,
         u.user_id,
         u.first_name,
         u.last_name
HAVING COUNT(DISTINCT r.reservation_id) = 1
ORDER BY c.province_name,
         c.city_name,
         u.user_id;


-- ============================================================
-- Query 5
-- اطلاعات کاربری که جدیدترین بلیط را خریداری کرده است.
-- User information of the user who bought the most recent ticket.
--
-- Definition:
-- The newest purchased ticket is determined by the latest successful
-- payment time.
-- ============================================================

SELECT u.user_id,
       u.first_name,
       u.last_name,
       u.email,
       u.phone_number,
       u.account_status,
       p.paid_at AS latest_purchase_time,
       t.ticket_id,
       m.match_id,
       m.match_title,
       s.sport_name,
       v.venue_name
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
         JOIN venues v
              ON v.venue_id = m.venue_id
WHERE r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
  AND p.paid_at IS NOT NULL
ORDER BY p.paid_at DESC LIMIT 1;


-- ============================================================
-- Query 6
-- شماره تلفن یا ایمیل کاربرانی که مجموع پرداخت‌های آنها
-- بیشتر از میانگین پرداخت کل کاربران است.
--
-- Interpretation:
-- Compare each user's total successful payment amount with the
-- average total successful payment amount per paying user.
-- ============================================================

WITH user_payment_totals AS (SELECT u.user_id,
                                    u.email,
                                    u.phone_number,
                                    SUM(p.amount) AS user_total_payment
                             FROM users u
                                      JOIN payments p
                                           ON p.user_id = u.user_id
                             WHERE p.payment_status = 'SUCCESS'
                             GROUP BY u.user_id,
                                      u.email,
                                      u.phone_number),
     average_payment AS (SELECT AVG(user_total_payment) AS average_total_payment
                         FROM user_payment_totals)
SELECT upt.user_id,
       COALESCE(upt.email, upt.phone_number) AS contact_info,
       upt.user_total_payment
FROM user_payment_totals upt
         CROSS JOIN average_payment ap
WHERE upt.user_total_payment > ap.average_total_payment
ORDER BY upt.user_total_payment DESC;


-- ============================================================
-- Query 7
-- تعداد بلیط‌های فروخته‌شده به ازای هر نوع مسابقه ورزشی.
-- Number of sold tickets for each sport type.
--
-- Definition of sold ticket:
-- reservation_status = 'PAID' and payment_status = 'SUCCESS'.
-- ============================================================

SELECT s.sport_id,
       s.sport_name,
       COUNT(DISTINCT t.ticket_id) AS sold_ticket_count
FROM sports s
         JOIN matches m
              ON m.sport_id = s.sport_id
         JOIN tickets t
              ON t.match_id = m.match_id
         JOIN reservations r
              ON r.ticket_id = t.ticket_id
         JOIN payments p
              ON p.reservation_id = r.reservation_id
WHERE r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
GROUP BY s.sport_id,
         s.sport_name
ORDER BY sold_ticket_count DESC;
