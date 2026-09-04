-- V29__add_verification_codes_table.sql
-- Email-verification codes: mirrors login_attempts (V3/V8). One row per issued
-- 6-digit code, BCrypt-hashed at rest, 15-minute TTL, per-code attempt counter.
CREATE TABLE verification_codes (
    verification_code_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     UUID         NOT NULL,
    code_hash   VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    consumed_at TIMESTAMP    NULL,
    attempts    INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_verification_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- Supports the rolling 1-hour generation COUNT per account.
CREATE INDEX idx_verification_codes_user_created
    ON verification_codes (user_id, created_at);

-- Supports the "latest unconsumed code for an account" lookup.
CREATE INDEX idx_verification_codes_user_active
    ON verification_codes (user_id, consumed_at, created_at);
