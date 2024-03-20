CREATE EXTENSION citext;

CREATE DOMAIN email AS citext
    CHECK ( value ~
            '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$' );

CREATE TABLE item_type
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cost                  NUMERIC(9, 2) NOT NULL CHECK (cost BETWEEN 0 AND 1000000.00),
    display_name          TEXT          NOT NULL,
    max_rent_time_minutes INT           NULL
);

CREATE TABLE item
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name TEXT NOT NULL,
    type_id      UUID NOT NULL REFERENCES item_type (id) ON DELETE CASCADE
);

CREATE TABLE "user"
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_type TEXT NOT NULL
);

CREATE TABLE human_user_passport
(
    yandex_id   BIGINT  NOT NULL UNIQUE,
    original_id UUID PRIMARY KEY REFERENCES "user" (id),
    first_name  TEXT    NOT NULL,
    last_name   TEXT    NOT NULL,
    email       email   NOT NULL,
    is_banned   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE lock_passport
(
    original_id UUID PRIMARY KEY REFERENCES "user" (id),
    item_id     UUID NULL REFERENCES item (id) UNIQUE
);

CREATE TABLE rent
(
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "from"   TIMESTAMP NOT NULL,
    "to"     TIMESTAMP NOT NULL,
    item_id  UUID      NOT NULL REFERENCES item (id),
    ended_at TIMESTAMP
);

CREATE TABLE wallet
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_yandex_id BIGINT NOT NULL REFERENCES human_user_passport (yandex_id) ON DELETE CASCADE,
    balance NUMERIC(9, 2) NOT NULL DEFAULT 0
);

CREATE TABLE transaction
(
    id           UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    amount       NUMERIC(9, 2) NOT NULL CHECK (amount BETWEEN 0 AND 1000000.00),
    is_success   BOOLEAN       NOT NULL,
    name         TEXT          NOT NULL,
    description  TEXT          NULL,
    committed_at TIMESTAMP     NOT NULL,
    wallet_id    UUID          NOT NULL REFERENCES wallet (id) ON DELETE CASCADE
);

CREATE TABLE role
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE user_role
(
    user_id UUID REFERENCES "user" (id),
    role_id SERIAL REFERENCES role (id),
    CONSTRAINT pk PRIMARY KEY (user_id, role_id)
);
