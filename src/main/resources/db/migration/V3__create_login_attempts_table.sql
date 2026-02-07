-- V3__Create_login_attempts_table.sql
CREATE TABLE login_attempts (
    login_attempt_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NULL,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(64),
    successful BOOLEAN NOT NULL,
    attempted_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);
