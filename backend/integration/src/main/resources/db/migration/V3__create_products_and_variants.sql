-- =========================================================
-- V3__create_products_and_variants.sql
-- Schema for the product catalog
-- =========================================================

-- ---------- Products Table ----------
-- This table stores the core product information, acting as an aggregate root.
CREATE TABLE IF NOT EXISTS products
(
    id          BIGSERIAL PRIMARY KEY,
    uuid        UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(120) NOT NULL,
    description TEXT,
    category    VARCHAR(60) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------- Product Variants Table ----------
-- This table stores the individual, sellable variations of a product.
CREATE TABLE IF NOT EXISTS product_variants
(
    id                BIGSERIAL PRIMARY KEY,
    uuid              UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    product_id        BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku               VARCHAR(100) UNIQUE NOT NULL,
    price_cents       INT NOT NULL,
    currency          VARCHAR(3) NOT NULL DEFAULT 'EUR',
    image_url         VARCHAR(255),
    stock_quantity    INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    is_available      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------- Indexes for Performance ----------
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category);
CREATE INDEX IF NOT EXISTS idx_product_variants_sku ON product_variants (sku);
CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON product_variants (product_id);


-- ---------- Trigger to update 'updated_at' timestamp ----------
-- A generic function to update the timestamp on any table with an 'updated_at' column.
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'set_updated_at') THEN
            CREATE OR REPLACE FUNCTION set_updated_at()
                RETURNS TRIGGER AS
            $f$
            BEGIN
                NEW.updated_at := NOW();
                RETURN NEW;
            END;
            $f$ LANGUAGE plpgsql;
        END IF;
    END
$$;

-- Apply the trigger to both new tables
CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE
    ON products
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_product_variants_updated_at
    BEFORE UPDATE
    ON product_variants
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();