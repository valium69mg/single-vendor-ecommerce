-- V28__add_slugs_and_slug_history.sql
--
-- Additive slug infrastructure for products, categories and brands.
--   * adds a NOT NULL UNIQUE `slug` column to each of the three tables
--   * backfills EVERY existing row (including soft-deleted ones) with a unique
--     slug derived from `name`, mirroring the Java-side SlugUtils algorithm
--     (accent folding via translate(), lowercase, non-alphanumeric run collapse,
--     edge-hyphen trim), disambiguating duplicates with a `-N` suffix and
--     falling back to `<entity>-<id>` when the name yields no usable slug
--   * creates one per-entity slug-history table with a real FK
--     (ON DELETE CASCADE) plus an index on every history FK
--
-- Definitive normalization lives in SlugUtils; the values written here are
-- stable once persisted. The script is additive only (ADD COLUMN / CREATE TABLE
-- / CREATE INDEX) and safe to re-run: backfill only touches rows whose slug is
-- still NULL and every constraint/index is guarded.

-- ─────────────────────────────────────────────────────────────────────────────
-- Categories
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE categories ADD COLUMN IF NOT EXISTS slug VARCHAR(255);

WITH computed AS (
    SELECT category_id,
           NULLIF(
               btrim(
                   regexp_replace(
                       lower(translate(COALESCE(name, ''),
                           'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÛÑÇáàäâãéèëêíìïîóòöôõúùüûñç',
                           'aaaaaeeeeiiiiooooouuuuncaaaaaeeeeiiiiooooouuuunc')),
                       '[^a-z0-9]+', '-', 'g'),
                   '-'),
               '') AS base
    FROM categories
),
ranked AS (
    SELECT category_id, base,
           row_number() OVER (PARTITION BY base ORDER BY category_id) AS rn
    FROM computed
)
UPDATE categories c
SET slug = CASE
              WHEN r.base IS NULL         THEN 'category-' || c.category_id
              WHEN r.rn = 1               THEN r.base
              ELSE r.base || '-' || r.rn
           END
FROM ranked r
WHERE c.category_id = r.category_id
  AND c.slug IS NULL;

ALTER TABLE categories ALTER COLUMN slug SET NOT NULL;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_categories_slug') THEN
        ALTER TABLE categories ADD CONSTRAINT uq_categories_slug UNIQUE (slug);
    END IF;
END $$;

-- ─────────────────────────────────────────────────────────────────────────────
-- Brands
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE brands ADD COLUMN IF NOT EXISTS slug VARCHAR(255);

WITH computed AS (
    SELECT brand_id,
           NULLIF(
               btrim(
                   regexp_replace(
                       lower(translate(COALESCE(name, ''),
                           'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÛÑÇáàäâãéèëêíìïîóòöôõúùüûñç',
                           'aaaaaeeeeiiiiooooouuuuncaaaaaeeeeiiiiooooouuuunc')),
                       '[^a-z0-9]+', '-', 'g'),
                   '-'),
               '') AS base
    FROM brands
),
ranked AS (
    SELECT brand_id, base,
           row_number() OVER (PARTITION BY base ORDER BY brand_id) AS rn
    FROM computed
)
UPDATE brands b
SET slug = CASE
              WHEN r.base IS NULL         THEN 'brand-' || b.brand_id
              WHEN r.rn = 1               THEN r.base
              ELSE r.base || '-' || r.rn
           END
FROM ranked r
WHERE b.brand_id = r.brand_id
  AND b.slug IS NULL;

ALTER TABLE brands ALTER COLUMN slug SET NOT NULL;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_brands_slug') THEN
        ALTER TABLE brands ADD CONSTRAINT uq_brands_slug UNIQUE (slug);
    END IF;
END $$;

-- ─────────────────────────────────────────────────────────────────────────────
-- Products
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE products ADD COLUMN IF NOT EXISTS slug VARCHAR(255);

WITH computed AS (
    SELECT product_id,
           NULLIF(
               btrim(
                   regexp_replace(
                       lower(translate(COALESCE(name, ''),
                           'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÛÑÇáàäâãéèëêíìïîóòöôõúùüûñç',
                           'aaaaaeeeeiiiiooooouuuuncaaaaaeeeeiiiiooooouuuunc')),
                       '[^a-z0-9]+', '-', 'g'),
                   '-'),
               '') AS base
    FROM products
),
ranked AS (
    SELECT product_id, base,
           row_number() OVER (PARTITION BY base ORDER BY product_id) AS rn
    FROM computed
)
UPDATE products p
SET slug = CASE
              WHEN r.base IS NULL         THEN 'product-' || p.product_id
              WHEN r.rn = 1               THEN r.base
              ELSE r.base || '-' || r.rn
           END
FROM ranked r
WHERE p.product_id = r.product_id
  AND p.slug IS NULL;

ALTER TABLE products ALTER COLUMN slug SET NOT NULL;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_products_slug') THEN
        ALTER TABLE products ADD CONSTRAINT uq_products_slug UNIQUE (slug);
    END IF;
END $$;

-- ─────────────────────────────────────────────────────────────────────────────
-- Slug-history tables (one per entity, real FK, ON DELETE CASCADE)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS category_slug_history (
    id         BIGSERIAL PRIMARY KEY,
    slug       VARCHAR(255) NOT NULL UNIQUE,
    category_id BIGINT      NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_category_slug_history_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_category_slug_history_category_id
    ON category_slug_history(category_id);

CREATE TABLE IF NOT EXISTS brand_slug_history (
    id         BIGSERIAL PRIMARY KEY,
    slug       VARCHAR(255) NOT NULL UNIQUE,
    brand_id   BIGINT       NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_brand_slug_history_brand
        FOREIGN KEY (brand_id) REFERENCES brands(brand_id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_brand_slug_history_brand_id
    ON brand_slug_history(brand_id);

CREATE TABLE IF NOT EXISTS product_slug_history (
    id         BIGSERIAL PRIMARY KEY,
    slug       VARCHAR(255) NOT NULL UNIQUE,
    product_id UUID         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_product_slug_history_product
        FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_product_slug_history_product_id
    ON product_slug_history(product_id);
