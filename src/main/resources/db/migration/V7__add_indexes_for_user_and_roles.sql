-- V7__add_indexes_for_user_and_roles.sql
-- Add indexes to optimize role and user queries

-- Index on user_roles.role_type to quickly filter by role
CREATE INDEX IF NOT EXISTS idx_user_roles_role_type
ON user_roles(role_type);

-- Index on users.user_role_id to optimize joins
CREATE INDEX IF NOT EXISTS idx_users_user_role_id
ON users(user_role_id);
