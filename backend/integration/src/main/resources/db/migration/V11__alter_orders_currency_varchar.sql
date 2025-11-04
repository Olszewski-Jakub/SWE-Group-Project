-- V11__alter_orders_currency_varchar.sql
-- Align orders.currency type with JPA mapping (varchar(3))

ALTER TABLE orders
    ALTER COLUMN currency TYPE varchar(3)
        USING currency::varchar(3);

