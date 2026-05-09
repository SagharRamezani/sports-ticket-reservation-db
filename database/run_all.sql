-- Run all database scripts in order

\i database/schema/01_tables.sql
\i database/schema/02_constraints.sql
\i database/schema/03_indexes.sql

\i database/seed/01_users_seed.sql
\i database/seed/02_sports_venues_teams_seed.sql
\i database/seed/03_matches_tickets_seed.sql
\i database/seed/04_reservations_payments_seed.sql
\i database/seed/05_reports_refunds_support_seed.sql

\i database/queries/q01_to_q07.sql
\i database/queries/q08_to_q15.sql
\i database/queries/q16_to_q22.sql

\i database/procedures/p01_to_p04.sql
\i database/procedures/p05_to_p08.sql