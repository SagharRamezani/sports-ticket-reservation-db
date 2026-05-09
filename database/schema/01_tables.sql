-- ============================================================
-- 01_tables.sql
-- Phase 1 - Initial table definitions
-- Project: Sports Match Ticket Reservation System
-- Owner: Saghar
-- Database: PostgreSQL
-- ============================================================

CREATE TABLE roles
(
    role_id     BIGINT GENERATED ALWAYS AS IDENTITY,
    role_code   VARCHAR(30)  NOT NULL,
    role_name   VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cities
(
    city_id       BIGINT GENERATED ALWAYS AS IDENTITY,
    city_name     VARCHAR(100) NOT NULL,
    province_name VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sports
(
    sport_id    BIGINT GENERATED ALWAYS AS IDENTITY,
    sport_code  VARCHAR(30)  NOT NULL,
    sport_name  VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users
(
    user_id           BIGINT GENERATED ALWAYS AS IDENTITY,
    role_id           BIGINT       NOT NULL,
    city_id           BIGINT,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    email             VARCHAR(255),
    phone_number      VARCHAR(20),
    password_hash     TEXT         NOT NULL,
    profile_image_url TEXT,
    account_status    VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    registered_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE TABLE venues
(
    venue_id       BIGINT GENERATED ALWAYS AS IDENTITY,
    city_id        BIGINT       NOT NULL,
    venue_name     VARCHAR(150) NOT NULL,
    address        TEXT,
    total_capacity INTEGER,
    venue_type     VARCHAR(50),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE teams
(
    team_id    BIGINT GENERATED ALWAYS AS IDENTITY,
    sport_id   BIGINT       NOT NULL,
    city_id    BIGINT,
    team_name  VARCHAR(150) NOT NULL,
    short_name VARCHAR(50),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE matches
(
    match_id         BIGINT GENERATED ALWAYS AS IDENTITY,
    sport_id         BIGINT       NOT NULL,
    venue_id         BIGINT       NOT NULL,
    home_team_id     BIGINT,
    away_team_id     BIGINT,
    match_title      VARCHAR(200) NOT NULL,
    tournament_name  VARCHAR(150),
    match_start_time TIMESTAMP    NOT NULL,
    match_status     VARCHAR(30)  NOT NULL DEFAULT 'SCHEDULED',
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE TABLE ticket_categories
(
    ticket_category_id BIGINT GENERATED ALWAYS AS IDENTITY,
    category_code      VARCHAR(50)  NOT NULL,
    category_name      VARCHAR(100) NOT NULL,
    description        TEXT,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tickets
(
    ticket_id          BIGINT GENERATED ALWAYS AS IDENTITY,
    match_id           BIGINT         NOT NULL,
    ticket_category_id BIGINT         NOT NULL,
    section_name       VARCHAR(100),
    row_number         VARCHAR(30),
    seat_number        VARCHAR(30),
    price              NUMERIC(12, 2) NOT NULL,
    ticket_status      VARCHAR(30)    NOT NULL DEFAULT 'AVAILABLE',
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP
);

CREATE TABLE reservations
(
    reservation_id      BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id             BIGINT      NOT NULL,
    ticket_id           BIGINT      NOT NULL,
    reservation_status  VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reserved_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at          TIMESTAMP   NOT NULL,
    confirmed_at        TIMESTAMP,
    cancelled_at        TIMESTAMP,
    cancellation_reason TEXT
);