CREATE SCHEMA IF NOT EXISTS hsse_tech;
SET SCHEMA 'hsse_tech';

DROP EXTENSION IF EXISTS citext CASCADE ;
CREATE EXTENSION citext;

DROP DOMAIN IF EXISTS email;

CREATE DOMAIN email AS citext
    CHECK ( value ~ '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$' );

CREATE TABLE IF NOT EXISTS item_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cost NUMERIC(9, 2) NOT NULL CHECK (cost BETWEEN 0 AND 1000000.00),
    display_name TEXT NOT NULL,
    max_rent_time_minutes INT NULL
);

CREATE TABLE IF NOT EXISTS item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name TEXT NOT NULL,
    type_id UUID NOT NULL REFERENCES item_type(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_type TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS human_user_passport (
    yandex_id BIGINT NOT NULL UNIQUE,
    original_id UUID PRIMARY KEY REFERENCES "user"(id),
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email email NOT NULL,
    is_banned BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS lock_passport (
    original_id UUID PRIMARY KEY REFERENCES "user"(id),
    item_id UUID NULL REFERENCES item(id) UNIQUE
);

CREATE TABLE IF NOT EXISTS rent (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "from" TIMESTAMP NOT NULL,
    "to" TIMESTAMP NOT NULL,
    item_id UUID NOT NULL REFERENCES item(id),
    ended_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS wallet (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_yandex_id BIGINT NOT NULL REFERENCES human_user_passport(yandex_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount NUMERIC(9, 2) NOT NULL CHECK (amount BETWEEN 0 AND 1000000.00),
    is_success BOOLEAN NOT NULL,
    name TEXT NOT NULL,
    description TEXT NULL,
    committed_at TIMESTAMP NOT NULL DEFAULT now(),
    wallet_id UUID NOT NULL REFERENCES wallet(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS role (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id UUID REFERENCES "user"(id),
    role_id SERIAL REFERENCES role(id),
    CONSTRAINT pk PRIMARY KEY (user_id, role_id)
);
