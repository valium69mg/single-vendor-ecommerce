-- V11__Create_categories_table.sql
CREATE TABLE categories (
    category_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE
);
