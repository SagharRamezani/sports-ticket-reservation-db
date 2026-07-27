-- ============================================================
-- 05_reports_refunds_support_seed.sql
-- Phase 2 - Seed data for refunds, report categories, reports,
-- support actions, and OTP logs
-- Project: Sports Match Ticket Reservation System
-- Owner: Shamim
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- 1. This file depends on:
--    database/seed/01_users_seed.sql
--    database/seed/02_sports_venues_teams_seed.sql
--    database/seed/03_matches_tickets_seed.sql
--    database/seed/04_reservations_payments_seed.sql
-- 2. Explicit IDs are used for stable test data.
-- 3. OVERRIDING SYSTEM VALUE is required because the schema uses
--    GENERATED ALWAYS AS IDENTITY.
-- 4. This file includes:
--    - Refund records for cancelled/refunded reservations
--    - Report categories for payment, seat, cancellation, schedule, and pricing issues
--    - Reports on different tickets and reservations
--    - Multiple reports for one ticket so Query 22 can return meaningful output
--    - Support actions by support users
--    - OTP logs with PENDING, VERIFIED, EXPIRED, and FAILED statuses

-- ============================================================
-- 1. Refunds
-- ============================================================

INSERT INTO refunds
    (
        refund_id,
        reservation_id,
        payment_id,
        amount,
        penalty_amount,
        refund_status,
        requested_at,
        processed_at,
        description
    )
OVERRIDING SYSTEM VALUE
VALUES
    (1, 13, 13, 315000.00, 35000.00, 'PROCESSED',
     CURRENT_TIMESTAMP - INTERVAL '2 days 1 hour 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '2 days 1 hour 20 minutes',
     'Refund processed after user cancellation for ticket 8.'),

    (2, 14, 14, 0.00, 0.00, 'REJECTED',
     CURRENT_TIMESTAMP - INTERVAL '1 day 6 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 day 6 hours 20 minutes',
     'Refund rejected because payment failed and no amount was captured.'),

    (3, 15, 15, 0.00, 0.00, 'REJECTED',
     CURRENT_TIMESTAMP - INTERVAL '3 days 1 hour 30 minutes',
     CURRENT_TIMESTAMP - INTERVAL '3 days 1 hour',
     'Expired reservation had no completed payment.'),

    (4, 7, 7, 225000.00, 25000.00, 'APPROVED',
     CURRENT_TIMESTAMP - INTERVAL '12 hours',
     NULL,
     'Refund approved for basketball ticket after user support request.'),

    (5, 2, 2, 960000.00, 240000.00, 'PENDING',
     CURRENT_TIMESTAMP - INTERVAL '3 hours',
     NULL,
     'Pending refund review for VIP ticket.')
ON CONFLICT (refund_id) DO UPDATE SET
    reservation_id = EXCLUDED.reservation_id,
    payment_id = EXCLUDED.payment_id,
    amount = EXCLUDED.amount,
    penalty_amount = EXCLUDED.penalty_amount,
    refund_status = EXCLUDED.refund_status,
    requested_at = EXCLUDED.requested_at,
    processed_at = EXCLUDED.processed_at,
    description = EXCLUDED.description;

-- ============================================================
-- 2. Report Categories
-- ============================================================

INSERT INTO report_categories
    (report_category_id, category_code, category_name, description, is_active, created_at)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'PAYMENT_ISSUE', 'Payment Issue', 'Problems related to failed, pending, or duplicate payments.', TRUE, CURRENT_TIMESTAMP),
    (2, 'SEAT_ISSUE', 'Seat Issue', 'Problems related to seat, row, section, or stand information.', TRUE, CURRENT_TIMESTAMP),
    (3, 'PRICE_ISSUE', 'Price Issue', 'Problems related to ticket pricing or discount calculation.', TRUE, CURRENT_TIMESTAMP),
    (4, 'CANCELLATION_ISSUE', 'Cancellation Issue', 'Problems related to cancellation or refund requests.', TRUE, CURRENT_TIMESTAMP),
    (5, 'SCHEDULE_CHANGE', 'Schedule Change', 'Problems related to match postponement or schedule change.', TRUE, CURRENT_TIMESTAMP),
    (6, 'VENUE_ISSUE', 'Venue Issue', 'Problems related to venue, entrance, or access.', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (report_category_id) DO UPDATE SET
    category_code = EXCLUDED.category_code,
    category_name = EXCLUDED.category_name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active;

-- ============================================================
-- 3. Reports
-- ============================================================

INSERT INTO reports
    (
        report_id,
        reporter_user_id,
        reservation_id,
        ticket_id,
        report_category_id,
        report_title,
        report_text,
        report_status,
        created_at,
        updated_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    -- Multiple reports for ticket 2 so Query 22 can find the most reported ticket
    (1, 2, 2, 2, 2,
     'VIP seat information mismatch',
     'The VIP row and entrance information for my ticket is not clear.',
     'OPEN',
     CURRENT_TIMESTAMP - INTERVAL '2 hours',
     NULL),

    (2, 5, 8, 2, 3,
     'VIP ticket price looks incorrect',
     'The final price shown for the VIP ticket seems higher than expected.',
     'IN_REVIEW',
     CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 hour'),

    (3, 1, 1, 2, 6,
     'VIP entrance access problem',
     'The dedicated entrance information for this ticket is missing.',
     'RESOLVED',
     CURRENT_TIMESTAMP - INTERVAL '1 day 1 hour',
     CURRENT_TIMESTAMP - INTERVAL '20 hours'),

    -- Reports for cancellation and refund workflows
    (4, 2, 13, 8, 4,
     'Cancellation refund delay',
     'My reservation was cancelled but I need confirmation about the refund.',
     'RESOLVED',
     CURRENT_TIMESTAMP - INTERVAL '2 days',
     CURRENT_TIMESTAMP - INTERVAL '1 day 20 hours'),

    (5, 3, 14, 17, 1,
     'Payment failed but reservation stayed active',
     'The payment failed and I need the support team to check the reservation status.',
     'CLOSED',
     CURRENT_TIMESTAMP - INTERVAL '1 day 7 hours',
     CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours'),

    (6, 4, 15, 12, 1,
     'Expired reservation payment question',
     'My reservation expired and I want to know if any payment was captured.',
     'REJECTED',
     CURRENT_TIMESTAMP - INTERVAL '3 days',
     CURRENT_TIMESTAMP - INTERVAL '2 days 20 hours'),

    -- Other report types
    (7, 8, 11, 3, 2,
     'Covered stand seat request',
     'I want to confirm whether my selected seat is in the covered stand.',
     'OPEN',
     CURRENT_TIMESTAMP - INTERVAL '25 minutes',
     NULL),

    (8, 9, 12, 11, 2,
     'Near court seat issue',
     'The selected volleyball seat is shown as near court but details are incomplete.',
     'OPEN',
     CURRENT_TIMESTAMP - INTERVAL '20 minutes',
     NULL),

    (9, 6, 9, 20, 5,
     'Match schedule confirmation',
     'Please confirm if the basketball match schedule has changed.',
     'IN_REVIEW',
     CURRENT_TIMESTAMP - INTERVAL '10 hours',
     CURRENT_TIMESTAMP - INTERVAL '8 hours'),

    (10, 7, 10, 14, 6,
     'Venue entrance guidance',
     'I need better guidance about the entrance for the Rasht venue.',
     'RESOLVED',
     CURRENT_TIMESTAMP - INTERVAL '8 days',
     CURRENT_TIMESTAMP - INTERVAL '7 days 20 hours')
ON CONFLICT (report_id) DO UPDATE SET
    reporter_user_id = EXCLUDED.reporter_user_id,
    reservation_id = EXCLUDED.reservation_id,
    ticket_id = EXCLUDED.ticket_id,
    report_category_id = EXCLUDED.report_category_id,
    report_title = EXCLUDED.report_title,
    report_text = EXCLUDED.report_text,
    report_status = EXCLUDED.report_status,
    created_at = EXCLUDED.created_at,
    updated_at = EXCLUDED.updated_at;

-- ============================================================
-- 4. Support Actions
-- ============================================================

INSERT INTO support_actions
    (
        support_action_id,
        report_id,
        reservation_id,
        support_user_id,
        action_type,
        action_note,
        created_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    (1, 1, 2, 11, 'REVIEW_REPORT',
     'Checked VIP ticket seat information and asked venue team for confirmation.',
     CURRENT_TIMESTAMP - INTERVAL '1 hour 50 minutes'),

    (2, 2, 8, 12, 'REVIEW_REPORT',
     'Reviewed price issue and compared final amount with VIP category price.',
     CURRENT_TIMESTAMP - INTERVAL '1 hour'),

    (3, 3, 1, 11, 'RESPOND_TO_USER',
     'Sent VIP entrance instructions to the user.',
     CURRENT_TIMESTAMP - INTERVAL '20 hours'),

    (4, 4, 13, 12, 'CANCEL_RESERVATION',
     'Confirmed cancellation and checked refund processing status.',
     CURRENT_TIMESTAMP - INTERVAL '1 day 22 hours'),

    (5, 5, 14, 11, 'CANCEL_RESERVATION',
     'Cancelled reservation after failed payment verification.',
     CURRENT_TIMESTAMP - INTERVAL '1 day 6 hours'),

    (6, 6, 15, 12, 'RESPOND_TO_USER',
     'Explained that expired reservation had no successful payment.',
     CURRENT_TIMESTAMP - INTERVAL '2 days 20 hours'),

    (7, 7, 11, 11, 'REVIEW_REPORT',
     'Started review for covered stand seat request.',
     CURRENT_TIMESTAMP - INTERVAL '15 minutes'),

    (8, 8, 12, 12, 'REVIEW_REPORT',
     'Started review for near court volleyball seat information.',
     CURRENT_TIMESTAMP - INTERVAL '10 minutes'),

    (9, 9, 9, 11, 'UPDATE_RESERVATION',
     'Updated support note for match schedule confirmation.',
     CURRENT_TIMESTAMP - INTERVAL '8 hours'),

    (10, 10, 10, 12, 'RESPOND_TO_USER',
     'Sent entrance guidance for Rasht venue.',
     CURRENT_TIMESTAMP - INTERVAL '7 days 20 hours')
ON CONFLICT (support_action_id) DO UPDATE SET
    report_id = EXCLUDED.report_id,
    reservation_id = EXCLUDED.reservation_id,
    support_user_id = EXCLUDED.support_user_id,
    action_type = EXCLUDED.action_type,
    action_note = EXCLUDED.action_note,
    created_at = EXCLUDED.created_at;

-- ============================================================
-- 5. OTP Logs
-- ============================================================

INSERT INTO otp_logs
    (
        otp_log_id,
        user_id,
        email,
        phone_number,
        otp_code_hash,
        otp_purpose,
        otp_status,
        expires_at,
        verified_at,
        created_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    (1, 1, 'ali.ahmadi@example.com', '09120000001', 'hash_otp_000001',
     'LOGIN', 'VERIFIED',
     CURRENT_TIMESTAMP - INTERVAL '1 day 23 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 day 23 hours 55 minutes',
     CURRENT_TIMESTAMP - INTERVAL '2 days'),

    (2, 2, 'sara.mohammadi@example.com', '09120000002', 'hash_otp_000002',
     'LOGIN', 'VERIFIED',
     CURRENT_TIMESTAMP - INTERVAL '2 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '2 hours 55 minutes',
     CURRENT_TIMESTAMP - INTERVAL '3 hours'),

    (3, 3, 'reza.karimi@example.com', '09120000003', 'hash_otp_000003',
     'PASSWORD_RESET', 'EXPIRED',
     CURRENT_TIMESTAMP - INTERVAL '1 day',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '1 day 10 minutes'),

    (4, 4, 'niloofar.hosseini@example.com', '09120000004', 'hash_otp_000004',
     'PROFILE_UPDATE', 'FAILED',
     CURRENT_TIMESTAMP - INTERVAL '4 hours',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '4 hours 10 minutes'),

    (5, 5, 'amir.moradi@example.com', '09120000005', 'hash_otp_000005',
     'LOGIN', 'PENDING',
     CURRENT_TIMESTAMP + INTERVAL '5 minutes',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '5 minutes'),

    (6, 6, 'mahsa.ebrahimi@example.com', '09120000006', 'hash_otp_000006',
     'SIGNUP', 'VERIFIED',
     CURRENT_TIMESTAMP - INTERVAL '20 days',
     CURRENT_TIMESTAMP - INTERVAL '20 days 5 minutes',
     CURRENT_TIMESTAMP - INTERVAL '20 days 10 minutes'),

    (7, 8, 'mina.jafari@example.com', '09120000008', 'hash_otp_000007',
     'LOGIN', 'PENDING',
     CURRENT_TIMESTAMP + INTERVAL '8 minutes',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '2 minutes'),

    (8, 9, 'parsa.sadeghi@example.com', '09120000009', 'hash_otp_000008',
     'LOGIN', 'EXPIRED',
     CURRENT_TIMESTAMP - INTERVAL '2 days',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '2 days 10 minutes'),

    (9, 11, 'shayan.support@example.com', '09120000011', 'hash_otp_000009',
     'LOGIN', 'VERIFIED',
     CURRENT_TIMESTAMP - INTERVAL '30 minutes',
     CURRENT_TIMESTAMP - INTERVAL '35 minutes',
     CURRENT_TIMESTAMP - INTERVAL '40 minutes'),

    (10, 12, 'negar.admin@example.com', '09120000012', 'hash_otp_000010',
     'LOGIN', 'VERIFIED',
     CURRENT_TIMESTAMP - INTERVAL '25 minutes',
     CURRENT_TIMESTAMP - INTERVAL '30 minutes',
     CURRENT_TIMESTAMP - INTERVAL '35 minutes')
ON CONFLICT (otp_log_id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    email = EXCLUDED.email,
    phone_number = EXCLUDED.phone_number,
    otp_code_hash = EXCLUDED.otp_code_hash,
    otp_purpose = EXCLUDED.otp_purpose,
    otp_status = EXCLUDED.otp_status,
    expires_at = EXCLUDED.expires_at,
    verified_at = EXCLUDED.verified_at,
    created_at = EXCLUDED.created_at;
