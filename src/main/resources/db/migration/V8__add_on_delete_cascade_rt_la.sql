-- V8__Add_cascade_delete_on_user_relations.sql

ALTER TABLE refresh_tokens
DROP CONSTRAINT fk_refresh_user,
ADD CONSTRAINT fk_refresh_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE;

ALTER TABLE login_attempts
DROP CONSTRAINT fk_login_user,
ADD CONSTRAINT fk_login_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE;
