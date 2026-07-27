-- ============================================================
-- 04_reservations_payments_seed.sql
-- Phase 2 - Seed data for reservations, payment methods, and payments
-- Project: Sports Match Ticket Reservation System
-- Owner: Shamim
-- Database: PostgreSQL
-- ============================================================

-- Notes:
-- 1. This file depends on:
--    database/seed/01_users_seed.sql
--    database/seed/02_sports_venues_teams_seed.sql
--    database/seed/03_matches_tickets_seed.sql
-- 2. Explicit IDs are used so later report/refund/support seeds can
--    reference stable reservation and payment IDs.
-- 3. OVERRIDING SYSTEM VALUE is required because the schema uses
--    GENERATED ALWAYS AS IDENTITY.
-- 4. The schema supports reservation_status values:
--    PENDING, PAID, CANCELLED, EXPIRED
--    Therefore, the business meaning of "RESERVED" is represented by
--    reservation_status = 'PENDING'.
-- 5. This file includes:
--    - Paid, pending, cancelled, and expired reservations
--    - Successful, failed, pending, cancelled, and refunded payments
--    - Purchases today, yesterday, and within the recent week
--    - Successful purchases in football, volleyball, and basketball
--    - Successful purchases in Tehran province
--    - User 10 intentionally still has no reservation for Query 1

-- ============================================================
-- 1. Payment Methods
-- ============================================================

INSERT INTO payment_methods
    (payment_method_id, method_code, method_name, description, is_active, created_at)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'BANK_CARD', 'Bank Card', 'Local bank card payment method.', TRUE, CURRENT_TIMESTAMP),
    (2, 'WALLET', 'Wallet', 'Internal user wallet payment method.', TRUE, CURRENT_TIMESTAMP),
    (3, 'CRYPTO', 'Crypto', 'Crypto payment method for optional testing.', TRUE, CURRENT_TIMESTAMP),
    (4, 'GIFT_CODE', 'Gift Code', 'Gift or discount code payment method.', TRUE, CURRENT_TIMESTAMP),
    (5, 'OFFLINE_TRANSFER', 'Offline Transfer', 'Manual offline transfer payment method.', FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (payment_method_id) DO UPDATE SET
    method_code = EXCLUDED.method_code,
    method_name = EXCLUDED.method_name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active;

-- ============================================================
-- 2. Reservations
-- ============================================================

INSERT INTO reservations
    (
        reservation_id,
        user_id,
        ticket_id,
        reservation_status,
        reserved_at,
        expires_at,
        confirmed_at,
        cancelled_at,
        cancellation_reason
    )
OVERRIDING SYSTEM VALUE
VALUES
    -- Successful purchases: yesterday, today, and recent week
    (1, 1, 1, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours',
     CURRENT_TIMESTAMP - INTERVAL '1 day 3 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 day 3 hours 50 minutes',
     NULL,
     NULL),

    (2, 2, 2, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '2 hours',
     CURRENT_TIMESTAMP - INTERVAL '1 hour 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 hour 50 minutes',
     NULL,
     NULL),

    (3, 3, 6, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '5 days',
     CURRENT_TIMESTAMP - INTERVAL '4 days 23 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '4 days 23 hours 50 minutes',
     NULL,
     NULL),

    (4, 4, 7, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '6 days',
     CURRENT_TIMESTAMP - INTERVAL '5 days 23 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '5 days 23 hours 50 minutes',
     NULL,
     NULL),

    (5, 1, 9, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '4 days',
     CURRENT_TIMESTAMP - INTERVAL '3 days 23 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '3 days 23 hours 50 minutes',
     NULL,
     NULL),

    (6, 2, 10, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '3 days',
     CURRENT_TIMESTAMP - INTERVAL '2 days 23 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '2 days 23 hours 50 minutes',
     NULL,
     NULL),

    (7, 1, 15, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '1 day 6 hours',
     CURRENT_TIMESTAMP - INTERVAL '1 day 5 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 day 5 hours 50 minutes',
     NULL,
     NULL),

    (8, 5, 16, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '6 hours',
     CURRENT_TIMESTAMP - INTERVAL '5 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '5 hours 50 minutes',
     NULL,
     NULL),

    (9, 6, 20, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '2 days',
     CURRENT_TIMESTAMP - INTERVAL '1 day 23 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 day 23 hours 50 minutes',
     NULL,
     NULL),

    (10, 7, 14, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '8 days',
     CURRENT_TIMESTAMP - INTERVAL '7 days 23 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '7 days 23 hours 50 minutes',
     NULL,
     NULL),

    -- Pending reservations: business meaning of temporary reserved tickets
    (11, 8, 3, 'PENDING',
     CURRENT_TIMESTAMP - INTERVAL '5 minutes',
     CURRENT_TIMESTAMP + INTERVAL '5 minutes',
     NULL,
     NULL,
     NULL),

    (12, 9, 11, 'PENDING',
     CURRENT_TIMESTAMP - INTERVAL '8 minutes',
     CURRENT_TIMESTAMP + INTERVAL '2 minutes',
     NULL,
     NULL,
     NULL),

    -- Cancelled reservations
    (13, 2, 8, 'CANCELLED',
     CURRENT_TIMESTAMP - INTERVAL '2 days 3 hours',
     CURRENT_TIMESTAMP - INTERVAL '2 days 2 hours 45 minutes',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '2 days 2 hours',
     'User requested cancellation after schedule change.'),

    (14, 3, 17, 'CANCELLED',
     CURRENT_TIMESTAMP - INTERVAL '1 day 8 hours',
     CURRENT_TIMESTAMP - INTERVAL '1 day 7 hours 45 minutes',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '1 day 7 hours',
     'Payment failed and reservation was cancelled.'),

    -- Expired reservation
    (15, 4, 12, 'EXPIRED',
     CURRENT_TIMESTAMP - INTERVAL '3 days 2 hours',
     CURRENT_TIMESTAMP - INTERVAL '3 days 1 hour 45 minutes',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '3 days 1 hour 40 minutes',
     'Reservation expired because payment was not completed.'),

    -- Additional successful purchase to support aggregation queries
    (16, 2, 4, 'PAID',
     CURRENT_TIMESTAMP - INTERVAL '6 days 4 hours',
     CURRENT_TIMESTAMP - INTERVAL '6 days 3 hours 45 minutes',
     CURRENT_TIMESTAMP - INTERVAL '6 days 3 hours 50 minutes',
     NULL,
     NULL)
ON CONFLICT (reservation_id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    ticket_id = EXCLUDED.ticket_id,
    reservation_status = EXCLUDED.reservation_status,
    reserved_at = EXCLUDED.reserved_at,
    expires_at = EXCLUDED.expires_at,
    confirmed_at = EXCLUDED.confirmed_at,
    cancelled_at = EXCLUDED.cancelled_at,
    cancellation_reason = EXCLUDED.cancellation_reason;

-- ============================================================
-- 3. Payments
-- ============================================================

INSERT INTO payments
    (
        payment_id,
        reservation_id,
        user_id,
        payment_method_id,
        amount,
        payment_status,
        transaction_reference,
        paid_at,
        created_at
    )
OVERRIDING SYSTEM VALUE
VALUES
    -- Successful payments
    (1, 1, 1, 1, 350000.00, 'SUCCESS', 'TXN-SUCCESS-0001',
     CURRENT_TIMESTAMP - INTERVAL '1 day 3 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 day 4 hours'),

    (2, 2, 2, 1, 1200000.00, 'SUCCESS', 'TXN-SUCCESS-0002',
     CURRENT_TIMESTAMP - INTERVAL '1 hour 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '2 hours'),

    (3, 3, 3, 2, 300000.00, 'SUCCESS', 'TXN-SUCCESS-0003',
     CURRENT_TIMESTAMP - INTERVAL '4 days 23 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '5 days'),

    (4, 4, 4, 1, 180000.00, 'SUCCESS', 'TXN-SUCCESS-0004',
     CURRENT_TIMESTAMP - INTERVAL '5 days 23 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '6 days'),

    (5, 5, 1, 2, 220000.00, 'SUCCESS', 'TXN-SUCCESS-0005',
     CURRENT_TIMESTAMP - INTERVAL '3 days 23 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '4 days'),

    (6, 6, 2, 1, 650000.00, 'SUCCESS', 'TXN-SUCCESS-0006',
     CURRENT_TIMESTAMP - INTERVAL '2 days 23 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '3 days'),

    (7, 7, 1, 1, 250000.00, 'SUCCESS', 'TXN-SUCCESS-0007',
     CURRENT_TIMESTAMP - INTERVAL '1 day 5 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '1 day 6 hours'),

    (8, 8, 5, 2, 800000.00, 'SUCCESS', 'TXN-SUCCESS-0008',
     CURRENT_TIMESTAMP - INTERVAL '5 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '6 hours'),

    (9, 9, 6, 1, 240000.00, 'SUCCESS', 'TXN-SUCCESS-0009',
     CURRENT_TIMESTAMP - INTERVAL '1 day 23 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '2 days'),

    (10, 10, 7, 4, 210000.00, 'SUCCESS', 'TXN-SUCCESS-0010',
     CURRENT_TIMESTAMP - INTERVAL '7 days 23 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '8 days'),

    -- Pending payments for temporary reservations
    (11, 11, 8, 1, 750000.00, 'PENDING', 'TXN-PENDING-0011',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '5 minutes'),

    (12, 12, 9, 2, 380000.00, 'PENDING', 'TXN-PENDING-0012',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '8 minutes'),

    -- Cancelled/refunded/failed payment examples
    (13, 13, 2, 1, 350000.00, 'REFUNDED', 'TXN-REFUNDED-0013',
     CURRENT_TIMESTAMP - INTERVAL '2 days 2 hours 30 minutes',
     CURRENT_TIMESTAMP - INTERVAL '2 days 3 hours'),

    (14, 14, 3, 1, 450000.00, 'FAILED', 'TXN-FAILED-0014',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '1 day 8 hours'),

    (15, 15, 4, 2, 240000.00, 'CANCELLED', 'TXN-CANCELLED-0015',
     NULL,
     CURRENT_TIMESTAMP - INTERVAL '3 days 2 hours'),

    -- Extra successful payment for top buyers / weekly queries
    (16, 16, 2, 1, 400000.00, 'SUCCESS', 'TXN-SUCCESS-0016',
     CURRENT_TIMESTAMP - INTERVAL '6 days 3 hours 50 minutes',
     CURRENT_TIMESTAMP - INTERVAL '6 days 4 hours')
ON CONFLICT (payment_id) DO UPDATE SET
    reservation_id = EXCLUDED.reservation_id,
    user_id = EXCLUDED.user_id,
    payment_method_id = EXCLUDED.payment_method_id,
    amount = EXCLUDED.amount,
    payment_status = EXCLUDED.payment_status,
    transaction_reference = EXCLUDED.transaction_reference,
    paid_at = EXCLUDED.paid_at,
    created_at = EXCLUDED.created_at;
