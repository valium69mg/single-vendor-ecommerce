ALTER TABLE users
ADD COLUMN user_role_id BIGINT NOT NULL,
ADD CONSTRAINT fk_user_role FOREIGN KEY (user_role_id) REFERENCES user_roles(id);
