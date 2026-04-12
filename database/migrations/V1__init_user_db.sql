CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email             VARCHAR(255),
    phone             VARCHAR(20)  NOT NULL,
    password_hash     VARCHAR(255) NOT NULL,
    full_name         VARCHAR(100) NOT NULL,
    nickname          VARCHAR(50),
    avatar_url        TEXT,
    bio               TEXT,
    role              VARCHAR(20)  NOT NULL DEFAULT 'user'
        CHECK (role IN ('user', 'moderator', 'admin')),
    email_verified_at TIMESTAMPTZ,
    phone_verified_at TIMESTAMPTZ,
    is_active         BOOLEAN NOT  NULL DEFAULT TRUE,
    last_login_at     TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ
);

CREATE UNIQUE INDEX users_email_uq
    ON users(email)
        WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX users_phone_uq
    ON users(phone)
        WHERE deleted_at IS NULL AND
            phone IS NOT NULL;

CREATE UNIQUE INDEX users_nickname_uq
    ON users(nickname)
        WHERE deleted_at IS NULL;

CREATE INDEX users_created_at_idx
    ON users(created_at);

CREATE INDEX users_deleted_at_idx
    ON users(deleted_at);

CREATE TABLE sessions (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash TEXT        NOT NULL,
    device_info        TEXT,
    expires_at         TIMESTAMPTZ NOT NULL,
    revoked_at         TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX sessions_user_id_idx
    ON sessions(user_id);

CREATE INDEX sessions_expires_at_idx
    ON sessions(expires_at);

CREATE INDEX sessions_revoked_at_idx
    ON sessions(revoked_at);

CREATE TABLE user_billing_profiles (
    id                        UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                   UUID          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    customer_id               VARCHAR(100),
    default_payment_method_id VARCHAR(100),
    created_at                TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_user_billing_profiles_updated_at
BEFORE UPDATE ON user_billing_profiles
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();