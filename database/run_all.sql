-- ============================================================
-- run_all.sql
-- Phase 2 - Execution order for Saghar's SQL files
-- Project: Sports Match Ticket Reservation System
-- Owner: Saghar
-- Database: PostgreSQL
--
-- Usage from project root:
-- psql -U postgres -d sports_ticket_db -f database/run_all.sql
-- ============================================================

\echo '============================================================'
\echo 'Phase 2 - Running schema files'
\echo '============================================================'

\i database/schema/01_tables.sql
\i database/schema/02_constraints.sql
\i database/schema/03_indexes.sql

\echo '============================================================'
\echo 'Phase 2 - Saghar analytical queries 1 to 7'
\echo '============================================================'

\i database/queries/q01_to_q07.sql

\echo '============================================================'
\echo 'Phase 2 - Saghar SQL files completed successfully'
\echo '============================================================'

-- ============================================================
-- Team files will be enabled after their branches are merged.
-- Keep these lines commented until the related files exist.
-- ============================================================

-- \echo '============================================================'
-- \echo 'Phase 2 - Seed data'
-- \echo '============================================================'
-- \i database/seed/01_users_seed.sql
-- \i database/seed/02_sports_venues_teams_seed.sql
-- \i database/seed/03_matches_tickets_seed.sql
-- \i database/seed/04_reservations_payments_seed.sql
-- \i database/seed/05_reports_refunds_support_seed.sql

-- \echo '============================================================'
-- \echo 'Phase 2 - Other analytical queries'
-- \echo '============================================================'
-- \i database/queries/q08_to_q15.sql
-- \i database/queries/q16_to_q22.sql

-- \echo '============================================================'
-- \echo 'Phase 2 - Stored procedures'
-- \echo '============================================================'
-- \i database/procedures/p01_to_p04.sql
-- \i database/procedures/p05_to_p08.sql
