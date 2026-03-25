-- V19__add_created_at_and_updated_at.sql

-- ------------------------
-- Add timestamps to attributes
-- ------------------------
ALTER TABLE attributes
ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE attributes
SET created_at = NOW(),
    updated_at = NOW();

ALTER TABLE attributes
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at SET NOT NULL;


-- ------------------------
-- Add timestamps to attribute_values
-- ------------------------
ALTER TABLE attribute_values
ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE attribute_values
SET created_at = NOW(),
    updated_at = NOW();

ALTER TABLE attribute_values
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at SET NOT NULL;


-- ------------------------
-- Add timestamps to product_variant_attributes
-- ------------------------
ALTER TABLE product_variant_attributes
ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE product_variant_attributes
SET created_at = NOW(),
    updated_at = NOW();

ALTER TABLE product_variant_attributes
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at SET NOT NULL;


-- ------------------------
-- Add timestamps to product_variants
-- ------------------------
ALTER TABLE product_variants
ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE product_variants
SET created_at = NOW(),
    updated_at = NOW();

ALTER TABLE product_variants
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at SET NOT NULL;

-- ------------------------
-- Add timestamps to brands
-- ------------------------
ALTER TABLE brands
ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE brands
SET created_at = NOW(),
    updated_at = NOW();

ALTER TABLE brands
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at SET NOT NULL;


-- ------------------------
-- Add timestamps to product_materials
-- ------------------------
ALTER TABLE product_materials
ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE product_materials
SET created_at = NOW(),
    updated_at = NOW();

ALTER TABLE product_materials
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at SET NOT NULL;


-- ------------------------
-- Add timestamps to materials
-- ------------------------
ALTER TABLE materials
ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE materials
SET created_at = NOW(),
    updated_at = NOW();

ALTER TABLE materials
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at SET NOT NULL;