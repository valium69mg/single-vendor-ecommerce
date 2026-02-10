-- V13__Create_brand_table.sql

CREATE TABLE brands (
    brand_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);
