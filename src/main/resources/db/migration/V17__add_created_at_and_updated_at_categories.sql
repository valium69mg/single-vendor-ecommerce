ALTER TABLE categories ADD COLUMN created_at TIMESTAMP;
ALTER TABLE categories ADD COLUMN updated_at TIMESTAMP;
UPDATE categories
SET created_at = NOW(),
    updated_at = NOW();
    ALTER TABLE categories
ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE categories
ALTER COLUMN updated_at SET NOT NULL;