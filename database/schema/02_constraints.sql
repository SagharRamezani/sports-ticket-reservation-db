-- ============================================================
-- 02_constraints.sql
-- Phase 2 - Primary keys, foreign keys, unique constraints,
-- and check constraints
-- Project: Sports Match Ticket Reservation System
-- Owner: Saghar
-- Database: PostgreSQL
-- ============================================================

-- ============================================================
-- 1. Primary Keys
-- ============================================================

ALTER TABLE roles
    ADD CONSTRAINT pk_roles PRIMARY KEY (role_id);
ALTER TABLE cities
    ADD CONSTRAINT pk_cities PRIMARY KEY (city_id);
ALTER TABLE sports
    ADD CONSTRAINT pk_sports PRIMARY KEY (sport_id);
ALTER TABLE users
    ADD CONSTRAINT pk_users PRIMARY KEY (user_id);
ALTER TABLE venues
    ADD CONSTRAINT pk_venues PRIMARY KEY (venue_id);
ALTER TABLE teams
    ADD CONSTRAINT pk_teams PRIMARY KEY (team_id);
ALTER TABLE matches
    ADD CONSTRAINT pk_matches PRIMARY KEY (match_id);
ALTER TABLE ticket_categories
    ADD CONSTRAINT pk_ticket_categories PRIMARY KEY (ticket_category_id);
ALTER TABLE tickets
    ADD CONSTRAINT pk_tickets PRIMARY KEY (ticket_id);
ALTER TABLE reservations
    ADD CONSTRAINT pk_reservations PRIMARY KEY (reservation_id);
ALTER TABLE payment_methods
    ADD CONSTRAINT pk_payment_methods PRIMARY KEY (payment_method_id);
ALTER TABLE payments
    ADD CONSTRAINT pk_payments PRIMARY KEY (payment_id);
ALTER TABLE cancellation_policies
    ADD CONSTRAINT pk_cancellation_policies PRIMARY KEY (cancellation_policy_id);
ALTER TABLE refunds
    ADD CONSTRAINT pk_refunds PRIMARY KEY (refund_id);
ALTER TABLE report_categories
    ADD CONSTRAINT pk_report_categories PRIMARY KEY (report_category_id);
ALTER TABLE reports
    ADD CONSTRAINT pk_reports PRIMARY KEY (report_id);
ALTER TABLE support_actions
    ADD CONSTRAINT pk_support_actions PRIMARY KEY (support_action_id);
ALTER TABLE otp_logs
    ADD CONSTRAINT pk_otp_logs PRIMARY KEY (otp_log_id);
ALTER TABLE features
    ADD CONSTRAINT pk_features PRIMARY KEY (feature_id);
ALTER TABLE ticket_features
    ADD CONSTRAINT pk_ticket_features PRIMARY KEY (ticket_id, feature_id);
ALTER TABLE football_details
    ADD CONSTRAINT pk_football_details PRIMARY KEY (football_detail_id);
ALTER TABLE volleyball_details
    ADD CONSTRAINT pk_volleyball_details PRIMARY KEY (volleyball_detail_id);
ALTER TABLE basketball_details
    ADD CONSTRAINT pk_basketball_details PRIMARY KEY (basketball_detail_id);

-- ============================================================
-- 2. Foreign Keys
-- ============================================================

ALTER TABLE users
    ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (role_id);
ALTER TABLE users
    ADD CONSTRAINT fk_users_city FOREIGN KEY (city_id) REFERENCES cities (city_id);
ALTER TABLE venues
    ADD CONSTRAINT fk_venues_city FOREIGN KEY (city_id) REFERENCES cities (city_id);
ALTER TABLE teams
    ADD CONSTRAINT fk_teams_sport FOREIGN KEY (sport_id) REFERENCES sports (sport_id);
ALTER TABLE teams
    ADD CONSTRAINT fk_teams_city FOREIGN KEY (city_id) REFERENCES cities (city_id);
ALTER TABLE matches
    ADD CONSTRAINT fk_matches_sport FOREIGN KEY (sport_id) REFERENCES sports (sport_id);
ALTER TABLE matches
    ADD CONSTRAINT fk_matches_venue FOREIGN KEY (venue_id) REFERENCES venues (venue_id);
ALTER TABLE matches
    ADD CONSTRAINT fk_matches_home_team FOREIGN KEY (home_team_id) REFERENCES teams (team_id);
ALTER TABLE matches
    ADD CONSTRAINT fk_matches_away_team FOREIGN KEY (away_team_id) REFERENCES teams (team_id);
ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_match FOREIGN KEY (match_id) REFERENCES matches (match_id);
ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_ticket_category FOREIGN KEY (ticket_category_id) REFERENCES ticket_categories (ticket_category_id);
ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (user_id);
ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (ticket_id);
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id);
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (user_id);
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods (payment_method_id);
ALTER TABLE cancellation_policies
    ADD CONSTRAINT fk_cancellation_policies_match FOREIGN KEY (match_id) REFERENCES matches (match_id);
ALTER TABLE cancellation_policies
    ADD CONSTRAINT fk_cancellation_policies_ticket_category FOREIGN KEY (ticket_category_id) REFERENCES ticket_categories (ticket_category_id);
ALTER TABLE refunds
    ADD CONSTRAINT fk_refunds_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id);
ALTER TABLE refunds
    ADD CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments (payment_id);
ALTER TABLE reports
    ADD CONSTRAINT fk_reports_reporter_user FOREIGN KEY (reporter_user_id) REFERENCES users (user_id);
ALTER TABLE reports
    ADD CONSTRAINT fk_reports_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id);
ALTER TABLE reports
    ADD CONSTRAINT fk_reports_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (ticket_id);
ALTER TABLE reports
    ADD CONSTRAINT fk_reports_report_category FOREIGN KEY (report_category_id) REFERENCES report_categories (report_category_id);
ALTER TABLE support_actions
    ADD CONSTRAINT fk_support_actions_report FOREIGN KEY (report_id) REFERENCES reports (report_id);
ALTER TABLE support_actions
    ADD CONSTRAINT fk_support_actions_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id);
ALTER TABLE support_actions
    ADD CONSTRAINT fk_support_actions_support_user FOREIGN KEY (support_user_id) REFERENCES users (user_id);
ALTER TABLE otp_logs
    ADD CONSTRAINT fk_otp_logs_user FOREIGN KEY (user_id) REFERENCES users (user_id);
ALTER TABLE ticket_features
    ADD CONSTRAINT fk_ticket_features_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (ticket_id);
ALTER TABLE ticket_features
    ADD CONSTRAINT fk_ticket_features_feature FOREIGN KEY (feature_id) REFERENCES features (feature_id);
ALTER TABLE football_details
    ADD CONSTRAINT fk_football_details_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (ticket_id);
ALTER TABLE volleyball_details
    ADD CONSTRAINT fk_volleyball_details_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (ticket_id);
ALTER TABLE basketball_details
    ADD CONSTRAINT fk_basketball_details_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (ticket_id);

-- ============================================================
-- 3. Unique Constraints
-- ============================================================

ALTER TABLE roles
    ADD CONSTRAINT uq_roles_role_code UNIQUE (role_code);
ALTER TABLE cities
    ADD CONSTRAINT uq_cities_city_province UNIQUE (city_name, province_name);
ALTER TABLE sports
    ADD CONSTRAINT uq_sports_sport_code UNIQUE (sport_code);
ALTER TABLE users
    ADD CONSTRAINT uq_users_email UNIQUE (email);
ALTER TABLE users
    ADD CONSTRAINT uq_users_phone_number UNIQUE (phone_number);
ALTER TABLE venues
    ADD CONSTRAINT uq_venues_city_name UNIQUE (city_id, venue_name);
ALTER TABLE teams
    ADD CONSTRAINT uq_teams_sport_team_name UNIQUE (sport_id, team_name);
ALTER TABLE ticket_categories
    ADD CONSTRAINT uq_ticket_categories_category_code UNIQUE (category_code);
ALTER TABLE payment_methods
    ADD CONSTRAINT uq_payment_methods_method_code UNIQUE (method_code);
ALTER TABLE payments
    ADD CONSTRAINT uq_payments_transaction_reference UNIQUE (transaction_reference);
ALTER TABLE report_categories
    ADD CONSTRAINT uq_report_categories_category_code UNIQUE (category_code);
ALTER TABLE features
    ADD CONSTRAINT uq_features_feature_code UNIQUE (feature_code);
ALTER TABLE football_details
    ADD CONSTRAINT uq_football_details_ticket UNIQUE (ticket_id);
ALTER TABLE volleyball_details
    ADD CONSTRAINT uq_volleyball_details_ticket UNIQUE (ticket_id);
ALTER TABLE basketball_details
    ADD CONSTRAINT uq_basketball_details_ticket UNIQUE (ticket_id);

-- ============================================================
-- 4. Check Constraints
-- ============================================================

ALTER TABLE users
    ADD CONSTRAINT chk_users_contact_required CHECK (email IS NOT NULL OR phone_number IS NOT NULL);
ALTER TABLE users
    ADD CONSTRAINT chk_users_account_status CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'));
ALTER TABLE venues
    ADD CONSTRAINT chk_venues_total_capacity CHECK (total_capacity IS NULL OR total_capacity > 0);
ALTER TABLE matches
    ADD CONSTRAINT chk_matches_different_teams CHECK (home_team_id IS NULL OR away_team_id IS NULL OR
                                                      home_team_id <> away_team_id);
ALTER TABLE matches
    ADD CONSTRAINT chk_matches_match_status CHECK (match_status IN ('SCHEDULED', 'POSTPONED', 'CANCELLED', 'FINISHED'));
ALTER TABLE tickets
    ADD CONSTRAINT chk_tickets_price CHECK (price >= 0);
ALTER TABLE tickets
    ADD CONSTRAINT chk_tickets_ticket_status CHECK (ticket_status IN
                                                    ('AVAILABLE', 'RESERVED', 'SOLD', 'CANCELLED', 'UNAVAILABLE'));
ALTER TABLE reservations
    ADD CONSTRAINT chk_reservations_status CHECK (reservation_status IN ('PENDING', 'PAID', 'CANCELLED', 'EXPIRED'));
ALTER TABLE reservations
    ADD CONSTRAINT chk_reservations_expiry_time CHECK (expires_at > reserved_at);
ALTER TABLE reservations
    ADD CONSTRAINT chk_reservations_confirmed_time CHECK (confirmed_at IS NULL OR confirmed_at >= reserved_at);
ALTER TABLE reservations
    ADD CONSTRAINT chk_reservations_cancelled_time CHECK (cancelled_at IS NULL OR cancelled_at >= reserved_at);
ALTER TABLE payments
    ADD CONSTRAINT chk_payments_amount CHECK (amount >= 0);
ALTER TABLE payments
    ADD CONSTRAINT chk_payments_status CHECK (payment_status IN
                                              ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED', 'REFUNDED'));
ALTER TABLE payments
    ADD CONSTRAINT chk_payments_paid_time CHECK (paid_at IS NULL OR paid_at >= created_at);
ALTER TABLE cancellation_policies
    ADD CONSTRAINT chk_cancellation_policies_hours CHECK (hours_before_match >= 0);
ALTER TABLE cancellation_policies
    ADD CONSTRAINT chk_cancellation_policies_penalty CHECK (penalty_percent >= 0 AND penalty_percent <= 100);
ALTER TABLE refunds
    ADD CONSTRAINT chk_refunds_amount CHECK (amount >= 0);
ALTER TABLE refunds
    ADD CONSTRAINT chk_refunds_penalty_amount CHECK (penalty_amount >= 0);
ALTER TABLE refunds
    ADD CONSTRAINT chk_refunds_status CHECK (refund_status IN ('PENDING', 'APPROVED', 'REJECTED', 'PROCESSED'));
ALTER TABLE refunds
    ADD CONSTRAINT chk_refunds_processed_time CHECK (processed_at IS NULL OR processed_at >= requested_at);
ALTER TABLE reports
    ADD CONSTRAINT chk_reports_related_object_required CHECK (reservation_id IS NOT NULL OR ticket_id IS NOT NULL);
ALTER TABLE reports
    ADD CONSTRAINT chk_reports_status CHECK (report_status IN ('OPEN', 'IN_REVIEW', 'RESOLVED', 'REJECTED', 'CLOSED'));
ALTER TABLE support_actions
    ADD CONSTRAINT chk_support_actions_related_object_required CHECK (report_id IS NOT NULL OR reservation_id IS NOT NULL);
ALTER TABLE support_actions
    ADD CONSTRAINT chk_support_actions_action_type CHECK (action_type IN
                                                          ('REVIEW_REPORT', 'CONFIRM_RESERVATION', 'CANCEL_RESERVATION',
                                                           'UPDATE_RESERVATION', 'RESPOND_TO_USER', 'OTHER'));
ALTER TABLE otp_logs
    ADD CONSTRAINT chk_otp_logs_contact_required CHECK (user_id IS NOT NULL OR email IS NOT NULL OR phone_number IS NOT NULL);
ALTER TABLE otp_logs
    ADD CONSTRAINT chk_otp_logs_purpose CHECK (otp_purpose IN ('LOGIN', 'SIGNUP', 'PROFILE_UPDATE', 'PASSWORD_RESET'));
ALTER TABLE otp_logs
    ADD CONSTRAINT chk_otp_logs_status CHECK (otp_status IN ('PENDING', 'VERIFIED', 'EXPIRED', 'FAILED'));
ALTER TABLE otp_logs
    ADD CONSTRAINT chk_otp_logs_expiry_time CHECK (expires_at > created_at);
