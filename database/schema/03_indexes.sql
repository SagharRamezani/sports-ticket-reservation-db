-- ============================================================
-- 03_indexes.sql
-- Phase 1 - Initial indexes for search, reservations, payments,
-- reports, and support workflows
-- Project: Sports Match Ticket Reservation System
-- Owner: Saghar
-- Database: PostgreSQL
-- ============================================================

-- ============================================================
-- 1. User search and authentication indexes
-- ============================================================

CREATE INDEX idx_users_role_id
    ON users (role_id);

CREATE INDEX idx_users_city_id
    ON users (city_id);

CREATE INDEX idx_users_account_status
    ON users (account_status);

CREATE INDEX idx_users_registered_at
    ON users (registered_at);

-- Note:
-- email and phone_number already have UNIQUE constraints in 02_constraints.sql.
-- PostgreSQL automatically creates indexes for UNIQUE constraints.

-- ============================================================
-- 2. Location, venue, sport, and team lookup indexes
-- ============================================================

CREATE INDEX idx_cities_province_name
    ON cities (province_name);

CREATE INDEX idx_venues_city_id
    ON venues (city_id);

CREATE INDEX idx_venues_venue_type
    ON venues (venue_type);

CREATE INDEX idx_teams_sport_id
    ON teams (sport_id);

CREATE INDEX idx_teams_city_id
    ON teams (city_id);

-- ============================================================
-- 3. Match search indexes
-- ============================================================

CREATE INDEX idx_matches_sport_id
    ON matches (sport_id);

CREATE INDEX idx_matches_venue_id
    ON matches (venue_id);

CREATE INDEX idx_matches_home_team_id
    ON matches (home_team_id);

CREATE INDEX idx_matches_away_team_id
    ON matches (away_team_id);

CREATE INDEX idx_matches_start_time
    ON matches (match_start_time);

CREATE INDEX idx_matches_status
    ON matches (match_status);

CREATE INDEX idx_matches_sport_start_time
    ON matches (sport_id, match_start_time);

CREATE INDEX idx_matches_venue_start_time
    ON matches (venue_id, match_start_time);

-- ============================================================
-- 4. Ticket search and availability indexes
-- ============================================================

CREATE INDEX idx_tickets_match_id
    ON tickets (match_id);

CREATE INDEX idx_tickets_ticket_category_id
    ON tickets (ticket_category_id);

CREATE INDEX idx_tickets_ticket_status
    ON tickets (ticket_status);

CREATE INDEX idx_tickets_price
    ON tickets (price);

CREATE INDEX idx_tickets_match_status
    ON tickets (match_id, ticket_status);

CREATE INDEX idx_tickets_match_category
    ON tickets (match_id, ticket_category_id);

CREATE INDEX idx_tickets_match_price
    ON tickets (match_id, price);

CREATE INDEX idx_tickets_section_row_seat
    ON tickets (section_name, row_number, seat_number);

-- ============================================================
-- 5. Ticket feature indexes
-- ============================================================

CREATE INDEX idx_ticket_features_ticket_id
    ON ticket_features (ticket_id);

CREATE INDEX idx_ticket_features_feature_id
    ON ticket_features (feature_id);

-- ============================================================
-- 6. Reservation indexes
-- ============================================================

CREATE INDEX idx_reservations_user_id
    ON reservations (user_id);

CREATE INDEX idx_reservations_ticket_id
    ON reservations (ticket_id);

CREATE INDEX idx_reservations_status
    ON reservations (reservation_status);

CREATE INDEX idx_reservations_reserved_at
    ON reservations (reserved_at);

CREATE INDEX idx_reservations_expires_at
    ON reservations (expires_at);

CREATE INDEX idx_reservations_user_status
    ON reservations (user_id, reservation_status);

CREATE INDEX idx_reservations_ticket_status
    ON reservations (ticket_id, reservation_status);

-- Useful for finding expired unpaid reservations.
CREATE INDEX idx_reservations_pending_expiry
    ON reservations (expires_at) WHERE reservation_status = 'PENDING';

-- ============================================================
-- 7. Payment indexes
-- ============================================================

CREATE INDEX idx_payments_reservation_id
    ON payments (reservation_id);

CREATE INDEX idx_payments_user_id
    ON payments (user_id);

CREATE INDEX idx_payments_payment_method_id
    ON payments (payment_method_id);

CREATE INDEX idx_payments_status
    ON payments (payment_status);

CREATE INDEX idx_payments_created_at
    ON payments (created_at);

CREATE INDEX idx_payments_paid_at
    ON payments (paid_at);

CREATE INDEX idx_payments_user_status
    ON payments (user_id, payment_status);

CREATE INDEX idx_payments_user_created_at
    ON payments (user_id, created_at);

-- transaction_reference already has a UNIQUE constraint in 02_constraints.sql.

-- ============================================================
-- 8. Cancellation policy and refund indexes
-- ============================================================

CREATE INDEX idx_cancellation_policies_match_id
    ON cancellation_policies (match_id);

CREATE INDEX idx_cancellation_policies_ticket_category_id
    ON cancellation_policies (ticket_category_id);

CREATE INDEX idx_cancellation_policies_hours_before_match
    ON cancellation_policies (hours_before_match);

CREATE INDEX idx_refunds_reservation_id
    ON refunds (reservation_id);

CREATE INDEX idx_refunds_payment_id
    ON refunds (payment_id);

CREATE INDEX idx_refunds_status
    ON refunds (refund_status);

CREATE INDEX idx_refunds_requested_at
    ON refunds (requested_at);

-- ============================================================
-- 9. Report and support workflow indexes
-- ============================================================

CREATE INDEX idx_reports_reporter_user_id
    ON reports (reporter_user_id);

CREATE INDEX idx_reports_reservation_id
    ON reports (reservation_id);

CREATE INDEX idx_reports_ticket_id
    ON reports (ticket_id);

CREATE INDEX idx_reports_report_category_id
    ON reports (report_category_id);

CREATE INDEX idx_reports_status
    ON reports (report_status);

CREATE INDEX idx_reports_created_at
    ON reports (created_at);

CREATE INDEX idx_reports_category_status
    ON reports (report_category_id, report_status);

CREATE INDEX idx_support_actions_report_id
    ON support_actions (report_id);

CREATE INDEX idx_support_actions_reservation_id
    ON support_actions (reservation_id);

CREATE INDEX idx_support_actions_support_user_id
    ON support_actions (support_user_id);

CREATE INDEX idx_support_actions_created_at
    ON support_actions (created_at);

-- ============================================================
-- 10. OTP log indexes
-- ============================================================

CREATE INDEX idx_otp_logs_user_id
    ON otp_logs (user_id);

CREATE INDEX idx_otp_logs_email
    ON otp_logs (email);

CREATE INDEX idx_otp_logs_phone_number
    ON otp_logs (phone_number);

CREATE INDEX idx_otp_logs_status
    ON otp_logs (otp_status);

CREATE INDEX idx_otp_logs_expires_at
    ON otp_logs (expires_at);

CREATE INDEX idx_otp_logs_purpose_status
    ON otp_logs (otp_purpose, otp_status);

-- Useful for finding active OTP records.
CREATE INDEX idx_otp_logs_pending_expiry
    ON otp_logs (expires_at) WHERE otp_status = 'PENDING';

-- ============================================================
-- 11. Sport-specific detail indexes
-- ============================================================

CREATE INDEX idx_football_details_ticket_id
    ON football_details (ticket_id);

CREATE INDEX idx_football_details_league_name
    ON football_details (league_name);

CREATE INDEX idx_volleyball_details_ticket_id
    ON volleyball_details (ticket_id);

CREATE INDEX idx_volleyball_details_league_name
    ON volleyball_details (league_name);

CREATE INDEX idx_basketball_details_ticket_id
    ON basketball_details (ticket_id);

CREATE INDEX idx_basketball_details_league_name
    ON basketball_details (league_name);