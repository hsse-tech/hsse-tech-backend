CREATE EXTENSION IF NOT EXISTS citext;

CREATE DOMAIN email AS citext
    CHECK ( value ~
            '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$' );

CREATE TYPE transaction_status AS ENUM ('IN_PROCESS', 'SUCCESS', 'FAILED');
CREATE CAST (varchar AS transaction_status) WITH INOUT AS IMPLICIT;

CREATE TABLE item_type
(
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cost                        NUMERIC(9, 2) NOT NULL CHECK (cost BETWEEN -1000000.00 AND 1000000.00),
    display_name                TEXT          NOT NULL UNIQUE,
    max_rent_time_minutes       INT           NULL,
    is_photo_required_on_finish BOOLEAN          DEFAULT FALSE
);

CREATE TABLE item
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name TEXT NOT NULL,
    type_id      UUID NOT NULL REFERENCES item_type (id) ON DELETE CASCADE
);

CREATE TABLE human_user_passport
(
    yandex_id   BIGINT  NOT NULL UNIQUE,
    original_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name  TEXT    NOT NULL,
    last_name   TEXT    NOT NULL,
    email       email   NOT NULL,
    is_banned   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE lock_passport
(
    original_id UUID PRIMARY KEY,
    item_id     UUID    NULL REFERENCES item (id) UNIQUE,
    is_open     BOOLEAN NOT NULL
);

CREATE TABLE rent
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "from"     TIMESTAMP NOT NULL,
    "to"       TIMESTAMP NOT NULL,
    item_id    UUID      NOT NULL REFERENCES item (id) ON DELETE CASCADE,
    started_at TIMESTAMP,
    ended_at   TIMESTAMP,
    user_id    UUID      NOT NULL REFERENCES human_user_passport (original_id) ON DELETE CASCADE
);

CREATE TABLE wallet
(
    id              UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    owner_yandex_id BIGINT        NOT NULL REFERENCES human_user_passport (yandex_id) ON DELETE CASCADE,
    balance         NUMERIC(9, 2) NOT NULL DEFAULT 0
);

CREATE TABLE transaction
(
    id          UUID PRIMARY KEY            DEFAULT gen_random_uuid(),
    amount      NUMERIC(9, 2)      NOT NULL CHECK (amount BETWEEN -1000000.00 AND 1000000.00),
    status      transaction_status NOT NULL DEFAULT 'IN_PROCESS',
    name        TEXT               NOT NULL,
    description TEXT               NULL,
    created_at  TIMESTAMP          NOT NULL DEFAULT now(),
    wallet_id   UUID               NOT NULL REFERENCES wallet (id) ON DELETE CASCADE
);

CREATE TABLE role
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE passport_role
(
    user_id UUID REFERENCES "human_user_passport" (original_id),
    role_id SERIAL REFERENCES role (id),
    CONSTRAINT pk PRIMARY KEY (user_id, role_id)
);

CREATE FUNCTION create_wallet() RETURNS TRIGGER
AS $$
    BEGIN
        INSERT INTO wallet (id, owner_yandex_id, balance) VALUES (gen_random_uuid(), NEW.yandex_id, 0);
        RETURN NEW;
        END;
    $$ LANGUAGE plpgsql;

CREATE TRIGGER on_user_created_wallet_create
    AFTER INSERT ON human_user_passport
    FOR EACH ROW
    EXECUTE FUNCTION create_wallet();
