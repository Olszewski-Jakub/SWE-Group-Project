-- =========================================================
-- V7__enable_trgm_and_indexes.sql
-- Text search support for product catalog
-- =========================================================

-- Install pg_trgm
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create trigram GIN indexes
CREATE INDEX IF NOT EXISTS idx_products_name_trgm
    ON products USING gin (name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_products_description_trgm
    ON products USING gin (description gin_trgm_ops);
