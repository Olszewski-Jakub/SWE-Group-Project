-- =========================================================
-- V8__add_variant_attributes.sql
-- Variant attributes as JSONB + index for filtering
-- =========================================================

-- Add flexible attributes storage for variants
ALTER TABLE product_variants
    ADD COLUMN IF NOT EXISTS attributes JSONB;

-- General-purpose GIN index for JSONB operators used in search
CREATE INDEX IF NOT EXISTS idx_product_variants_attributes_gin
    ON product_variants USING gin (attributes);