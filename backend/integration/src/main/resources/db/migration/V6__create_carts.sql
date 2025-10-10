-- =========================================================
-- V6__create_carts.sql
-- Shopping Cart Schema aligned with Java Entities
-- =========================================================

-- ---------- ENUM for Cart Status ----------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'cart_status') THEN
CREATE TYPE cart_status AS ENUM ('ACTIVE','CHECKED_OUT','ABANDONED');
END IF;
END $$;

-- ---------- Shopping Carts Table ----------
CREATE TABLE IF NOT EXISTS shopping_carts
(
    uuid        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    status      cart_status NOT NULL DEFAULT 'ACTIVE',
    currency    CHAR(3) NOT NULL DEFAULT 'EUR',
    created_at  BIGINT NOT NULL,
    updated_at  BIGINT NOT NULL
    );

-- ---------- Cart Items Table ----------
CREATE TABLE IF NOT EXISTS cart_items
(
    uuid         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_uuid    UUID NOT NULL REFERENCES shopping_carts(uuid) ON DELETE CASCADE,
    variant_id   UUID NOT NULL,
    quantity     INT NOT NULL CHECK (quantity > 0),
    UNIQUE(cart_uuid, variant_id)
    );

-- =========================================================
-- INDEXES
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_uuid ON cart_items(cart_uuid);
CREATE INDEX IF NOT EXISTS idx_cart_items_variant_id ON cart_items(variant_id);
CREATE INDEX IF NOT EXISTS idx_shopping_carts_user_id ON shopping_carts(user_id);

-- =========================================================
-- TRIGGERS
-- =========================================================

-- ---------- Update shopping_cart.updated_at when items change ----------
CREATE OR REPLACE FUNCTION trg_update_cart_timestamp()
RETURNS TRIGGER AS $$
BEGIN
UPDATE shopping_carts
SET updated_at = (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
WHERE uuid = NEW.cart_uuid;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cart_items_updated_at
    AFTER INSERT OR UPDATE OR DELETE ON cart_items
    FOR EACH ROW
    EXECUTE FUNCTION trg_update_cart_timestamp();
