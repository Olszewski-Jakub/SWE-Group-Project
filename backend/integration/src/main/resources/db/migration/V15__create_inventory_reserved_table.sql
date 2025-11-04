-- V15__create_inventory_reserved_table.sql
-- Decouple reservation counters from product_variants; keep only total stock

CREATE TABLE IF NOT EXISTS inventory (
    variant_id UUID PRIMARY KEY,
    reserved INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed rows for existing variants
INSERT INTO inventory (variant_id, reserved)
SELECT v.uuid, 0
FROM product_variants v
ON CONFLICT (variant_id) DO NOTHING;

