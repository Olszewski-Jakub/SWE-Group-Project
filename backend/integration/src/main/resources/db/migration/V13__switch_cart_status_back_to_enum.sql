-- V13__switch_cart_status_back_to_enum.sql
-- Convert shopping_carts.status back to PostgreSQL enum cart_status

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'cart_status') THEN
        CREATE TYPE cart_status AS ENUM ('ACTIVE','CHECKED_OUT','ABANDONED');
    END IF;
END $$;

ALTER TABLE shopping_carts
    ALTER COLUMN status TYPE cart_status
        USING status::cart_status;

