-- ============================
-- V1__init_auth.sql
-- All auth & authorization schema in one migration (PostgreSQL)
-- ============================

-- ---------- Extensions & Enums ----------
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS citext; -- case-insensitive text

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_status') THEN
            CREATE TYPE user_status AS ENUM ('ACTIVE','LOCKED','DELETED');
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'oauth_provider') THEN
            CREATE TYPE oauth_provider AS ENUM ('GOOGLE');
        END IF;
    END
$$;

-- ---------- Core Identity ----------
CREATE TABLE IF NOT EXISTS users
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    email          CITEXT      NOT NULL UNIQUE,
    email_verified BOOLEAN     NOT NULL DEFAULT FALSE,
    password_hash  TEXT, -- NULL for social-only accounts
    first_name     TEXT,
    last_name      TEXT,
    status         user_status NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_status ON users (status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users (created_at);

CREATE TABLE IF NOT EXISTS roles
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ---------- Sessions (Refresh Tokens) ----------
CREATE TABLE IF NOT EXISTS sessions
(
    id                     UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id                UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    refresh_token_hash     TEXT        NOT NULL, -- store HASH ONLY
    user_agent             TEXT,
    ip_address             INET,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at             TIMESTAMPTZ NOT NULL,
    revoked_at             TIMESTAMPTZ,
    replaced_by_session_id UUID        REFERENCES sessions (id) ON DELETE SET NULL,
    reason                 TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_sessions_refresh_hash ON sessions (refresh_token_hash);
CREATE INDEX IF NOT EXISTS idx_sessions_user ON sessions (user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_expires ON sessions (expires_at);
CREATE INDEX IF NOT EXISTS idx_sessions_revoked ON sessions (revoked_at);

-- ---------- OAuth Accounts ----------
CREATE TABLE IF NOT EXISTS oauth_accounts
(
    id               UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    user_id          UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider         oauth_provider NOT NULL,
    provider_user_id TEXT           NOT NULL,
    email            CITEXT,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (provider, provider_user_id),
    UNIQUE (provider, user_id)
);

-- ---------- Email Verification & Password Reset ----------
CREATE TABLE IF NOT EXISTS email_verification_tokens
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash TEXT        NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    used_at    TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_email_verif_token_hash ON email_verification_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_email_verif_user ON email_verification_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_email_verif_expires ON email_verification_tokens (expires_at);

CREATE TABLE IF NOT EXISTS password_reset_tokens
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash TEXT        NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    used_at    TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_pwd_reset_token_hash ON password_reset_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_pwd_reset_user ON password_reset_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_pwd_reset_expires ON password_reset_tokens (expires_at);

-- ---------- Audit Events ----------
CREATE TABLE IF NOT EXISTS audit_events
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id    UUID        REFERENCES users (id) ON DELETE SET NULL,
    event_type TEXT        NOT NULL, -- e.g., LOGIN_SUCCESS, LOGIN_FAILED, REFRESH, LOGOUT, REGISTER
    metadata   JSONB,                -- IP, UA, sessionId, etc.
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_events (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_type ON audit_events (event_type);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_events (created_at);

-- ---------- Seed Roles ----------
INSERT INTO roles (id, name)
VALUES (gen_random_uuid(), 'CUSTOMER'),
       (gen_random_uuid(), 'STAFF'),
       (gen_random_uuid(), 'MANAGER'),
       (gen_random_uuid(), 'ADMIN'),
       (gen_random_uuid(), 'READONLY_SUPPORT')
ON CONFLICT (name) DO NOTHING;

-- ---------- Triggers (optional: updated_at) ----------
-- Keep updated_at fresh on users
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'set_users_updated_at') THEN
            CREATE OR REPLACE FUNCTION set_users_updated_at()
                RETURNS TRIGGER AS
            $f$
            BEGIN
                NEW.updated_at := NOW();
                RETURN NEW;
            END;
            $f$ LANGUAGE plpgsql;

            CREATE TRIGGER trg_users_updated_at
                BEFORE UPDATE
                ON users
                FOR EACH ROW
            EXECUTE FUNCTION set_users_updated_at();
        END IF;
    END
$$;
