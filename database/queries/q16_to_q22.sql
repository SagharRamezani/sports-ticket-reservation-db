-- ============================================================
-- q16_to_q22.sql
-- Phase 2 - Analytical and Data Modification Queries 16 to 22
-- Project: Sports Match Ticket Reservation System
-- Owner: Sarina
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- 1. Successful purchases are represented by:
--    reservations.reservation_status = 'PAID'
--    payments.payment_status = 'SUCCESS'
-- 2. Cancelled tickets are interpreted through cancelled reservations
--    and/or tickets with ticket_status = 'CANCELLED'.
-- 3. Queries 18 to 21 are destructive or data-changing queries.
--    Run them only on a test database or inside a transaction.
-- 4. Because each physical ticket is usually sold once, Query 16
--    interprets "best-selling ticket" as "best-selling ticket category".

-- ============================================================
-- Query 16
-- دومین بلیط پرفروش در بین کل بلیط‌ها.
-- Second best-selling ticket category in the system.
-- ============================================================

WITH ticket_category_sales AS (
    SELECT
        tc.ticket_category_id,
        tc.category_code,
        tc.category_name,
        COUNT(DISTINCT r.reservation_id) AS sold_count
    FROM ticket_categories tc
    JOIN tickets t
        ON t.ticket_category_id = tc.ticket_category_id
    JOIN reservations r
        ON r.ticket_id = t.ticket_id
    JOIN payments p
        ON p.reservation_id = r.reservation_id
    WHERE r.reservation_status = 'PAID'
      AND p.payment_status = 'SUCCESS'
    GROUP BY
        tc.ticket_category_id,
        tc.category_code,
        tc.category_name
),
ranked_sales AS (
    SELECT
        ticket_category_id,
        category_code,
        category_name,
        sold_count,
        DENSE_RANK() OVER (ORDER BY sold_count DESC) AS sales_rank
    FROM ticket_category_sales
)
SELECT
    ticket_category_id,
    category_code,
    category_name,
    sold_count
FROM ranked_sales
WHERE sales_rank = 2
ORDER BY
    category_name ASC;

-- ============================================================
-- Query 17
-- نام پشتیبان با بیشترین تعداد لغو رزرو بلیط همراه با درصد لغوها.
-- Support user with the highest number of cancelled reservations
-- and their cancellation percentage among all support cancellation actions.
-- ============================================================

WITH support_cancellations AS (
    SELECT
        u.user_id AS support_user_id,
        u.first_name,
        u.last_name,
        COALESCE(u.email, u.phone_number) AS support_contact,
        COUNT(DISTINCT sa.reservation_id) AS cancelled_reservation_count
    FROM support_actions sa
    JOIN users u
        ON u.user_id = sa.support_user_id
    JOIN roles ro
        ON ro.role_id = u.role_id
    JOIN reservations r
        ON r.reservation_id = sa.reservation_id
    WHERE ro.role_code = 'SUPPORT'
      AND sa.action_type = 'CANCEL_RESERVATION'
      AND r.reservation_status = 'CANCELLED'
    GROUP BY
        u.user_id,
        u.first_name,
        u.last_name,
        COALESCE(u.email, u.phone_number)
),
total_cancellations AS (
    SELECT
        SUM(cancelled_reservation_count) AS total_cancelled_reservations
    FROM support_cancellations
)
SELECT
    sc.support_user_id,
    sc.first_name,
    sc.last_name,
    sc.support_contact,
    sc.cancelled_reservation_count,
    ROUND(
        (sc.cancelled_reservation_count * 100.0)
        / NULLIF(tc.total_cancelled_reservations, 0),
        2
    ) AS cancellation_percentage
FROM support_cancellations sc
CROSS JOIN total_cancellations tc
ORDER BY
    sc.cancelled_reservation_count DESC,
    sc.support_user_id ASC
LIMIT 1;

-- ============================================================
-- Query 18
-- هشدار: این query داده را تغییر می‌دهد.
-- نام خانوادگی کاربری که بیشترین تعداد بلیط کنسل‌شده دارد
-- را به "Redington" تغییر بده.
-- ============================================================

WITH cancelled_ticket_counts AS (
    SELECT
        u.user_id,
        COUNT(DISTINCT r.ticket_id) AS cancelled_ticket_count
    FROM users u
    JOIN reservations r
        ON r.user_id = u.user_id
    JOIN tickets t
        ON t.ticket_id = r.ticket_id
    WHERE r.reservation_status = 'CANCELLED'
       OR t.ticket_status = 'CANCELLED'
    GROUP BY
        u.user_id
),
top_cancelled_user AS (
    SELECT
        user_id
    FROM cancelled_ticket_counts
    ORDER BY
        cancelled_ticket_count DESC,
        user_id ASC
    LIMIT 1
)
UPDATE users u
SET
    last_name = 'Redington',
    updated_at = CURRENT_TIMESTAMP
FROM top_cancelled_user tcu
WHERE u.user_id = tcu.user_id
RETURNING
    u.user_id,
    u.first_name,
    u.last_name,
    u.updated_at;

-- ============================================================
-- Query 19
-- هشدار: این query داده را حذف می‌کند.
-- تمام بلیط‌های کنسل‌شده کاربر Redington را حذف کن.
-- This script deletes dependent rows first to avoid FK violations.
-- ============================================================

BEGIN;

CREATE TEMP TABLE tmp_redington_cancelled_tickets ON COMMIT DROP AS
SELECT DISTINCT
    t.ticket_id
FROM users u
JOIN reservations r
    ON r.user_id = u.user_id
JOIN tickets t
    ON t.ticket_id = r.ticket_id
WHERE u.last_name = 'Redington'
  AND (
        r.reservation_status = 'CANCELLED'
        OR t.ticket_status = 'CANCELLED'
      );

CREATE TEMP TABLE tmp_redington_cancelled_reservations ON COMMIT DROP AS
SELECT DISTINCT
    r.reservation_id
FROM reservations r
JOIN tmp_redington_cancelled_tickets tt
    ON tt.ticket_id = r.ticket_id;

CREATE TEMP TABLE tmp_redington_cancelled_payments ON COMMIT DROP AS
SELECT DISTINCT
    p.payment_id
FROM payments p
JOIN tmp_redington_cancelled_reservations tr
    ON tr.reservation_id = p.reservation_id;

DELETE FROM support_actions
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_redington_cancelled_reservations
)
   OR report_id IN (
        SELECT report_id
        FROM reports
        WHERE reservation_id IN (
            SELECT reservation_id FROM tmp_redington_cancelled_reservations
        )
           OR ticket_id IN (
            SELECT ticket_id FROM tmp_redington_cancelled_tickets
        )
   );

DELETE FROM refunds
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_redington_cancelled_reservations
)
   OR payment_id IN (
        SELECT payment_id FROM tmp_redington_cancelled_payments
   );

DELETE FROM reports
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_redington_cancelled_reservations
)
   OR ticket_id IN (
        SELECT ticket_id FROM tmp_redington_cancelled_tickets
   );

DELETE FROM payments
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_redington_cancelled_reservations
);

DELETE FROM reservations
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_redington_cancelled_reservations
);

DELETE FROM ticket_features
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_redington_cancelled_tickets
);

DELETE FROM football_details
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_redington_cancelled_tickets
);

DELETE FROM volleyball_details
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_redington_cancelled_tickets
);

DELETE FROM basketball_details
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_redington_cancelled_tickets
);

DELETE FROM tickets
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_redington_cancelled_tickets
)
RETURNING
    ticket_id,
    match_id,
    ticket_category_id,
    ticket_status;

COMMIT;

-- ============================================================
-- Query 20
-- هشدار: این query داده را حذف می‌کند.
-- تمام بلیط‌های کنسل‌شده در سیستم را پاک کن.
-- This script deletes dependent rows first to avoid FK violations.
-- ============================================================

BEGIN;

CREATE TEMP TABLE tmp_all_cancelled_tickets ON COMMIT DROP AS
SELECT DISTINCT
    t.ticket_id
FROM tickets t
LEFT JOIN reservations r
    ON r.ticket_id = t.ticket_id
WHERE t.ticket_status = 'CANCELLED'
   OR r.reservation_status = 'CANCELLED';

CREATE TEMP TABLE tmp_all_cancelled_reservations ON COMMIT DROP AS
SELECT DISTINCT
    r.reservation_id
FROM reservations r
JOIN tmp_all_cancelled_tickets tt
    ON tt.ticket_id = r.ticket_id;

CREATE TEMP TABLE tmp_all_cancelled_payments ON COMMIT DROP AS
SELECT DISTINCT
    p.payment_id
FROM payments p
JOIN tmp_all_cancelled_reservations tr
    ON tr.reservation_id = p.reservation_id;

DELETE FROM support_actions
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_all_cancelled_reservations
)
   OR report_id IN (
        SELECT report_id
        FROM reports
        WHERE reservation_id IN (
            SELECT reservation_id FROM tmp_all_cancelled_reservations
        )
           OR ticket_id IN (
            SELECT ticket_id FROM tmp_all_cancelled_tickets
        )
   );

DELETE FROM refunds
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_all_cancelled_reservations
)
   OR payment_id IN (
        SELECT payment_id FROM tmp_all_cancelled_payments
   );

DELETE FROM reports
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_all_cancelled_reservations
)
   OR ticket_id IN (
        SELECT ticket_id FROM tmp_all_cancelled_tickets
   );

DELETE FROM payments
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_all_cancelled_reservations
);

DELETE FROM reservations
WHERE reservation_id IN (
    SELECT reservation_id FROM tmp_all_cancelled_reservations
);

DELETE FROM ticket_features
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_all_cancelled_tickets
);

DELETE FROM football_details
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_all_cancelled_tickets
);

DELETE FROM volleyball_details
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_all_cancelled_tickets
);

DELETE FROM basketball_details
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_all_cancelled_tickets
);

DELETE FROM tickets
WHERE ticket_id IN (
    SELECT ticket_id FROM tmp_all_cancelled_tickets
)
RETURNING
    ticket_id,
    match_id,
    ticket_category_id,
    ticket_status;

COMMIT;

-- ============================================================
-- Query 21
-- هشدار: این query قیمت بلیط‌ها را تغییر می‌دهد.
-- قیمت بلیط‌هایی که دیروز برای مسابقات برگزارشده در ورزشگاه آزادی
-- فروخته شده‌اند را ۱۰٪ کاهش بده.
-- ============================================================

UPDATE tickets t
SET
    price = ROUND(t.price * 0.90, 2),
    updated_at = CURRENT_TIMESTAMP
FROM reservations r
JOIN payments p
    ON p.reservation_id = r.reservation_id
JOIN matches m
    ON m.match_id = t.match_id
JOIN venues v
    ON v.venue_id = m.venue_id
WHERE t.ticket_id = r.ticket_id
  AND r.reservation_status = 'PAID'
  AND p.payment_status = 'SUCCESS'
  AND p.paid_at::DATE = CURRENT_DATE - 1
  AND LOWER(v.venue_name) = LOWER('Azadi Stadium')
RETURNING
    t.ticket_id,
    t.match_id,
    t.price AS new_price,
    t.updated_at;

-- ============================================================
-- Query 22
-- موضوع و تعداد گزارش‌ها را برای بلیط با بیشترین تعداد گزارش نمایش بده.
-- Show report subject and report count for the ticket with the highest
-- number of reports.
-- ============================================================

WITH ticket_report_counts AS (
    SELECT
        ticket_id,
        COUNT(*) AS total_report_count,
        DENSE_RANK() OVER (ORDER BY COUNT(*) DESC) AS report_rank
    FROM reports
    WHERE ticket_id IS NOT NULL
    GROUP BY
        ticket_id
),
top_reported_tickets AS (
    SELECT
        ticket_id,
        total_report_count
    FROM ticket_report_counts
    WHERE report_rank = 1
)
SELECT
    trt.ticket_id,
    r.report_title AS report_subject,
    COUNT(*) AS subject_report_count,
    trt.total_report_count AS total_reports_for_ticket
FROM top_reported_tickets trt
JOIN reports r
    ON r.ticket_id = trt.ticket_id
GROUP BY
    trt.ticket_id,
    r.report_title,
    trt.total_report_count
ORDER BY
    subject_report_count DESC,
    r.report_title ASC;
