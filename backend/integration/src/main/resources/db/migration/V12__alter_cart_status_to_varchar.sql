-- V12__alter_cart_status_to_varchar.sql
-- Switch shopping_carts.status from Postgres enum to varchar for JPA compatibility

ALTER TABLE shopping_carts
    ALTER COLUMN status TYPE varchar(32)
        USING status::text;

-- Optional: keep the enum type to avoid breaking other objects.
-- DROP TYPE IF EXISTS cart_status;

