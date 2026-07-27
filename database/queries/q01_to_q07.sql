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
