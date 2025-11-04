-- V14__add_order_shipping_columns.sql

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS shipping_rate_id VARCHAR(128),
  ADD COLUMN IF NOT EXISTS shipping_amount_minor BIGINT,
  ADD COLUMN IF NOT EXISTS shipping_currency VARCHAR(3),
  ADD COLUMN IF NOT EXISTS shipping_name VARCHAR(255),
  ADD COLUMN IF NOT EXISTS shipping_phone VARCHAR(64),
  ADD COLUMN IF NOT EXISTS shipping_address_line1 VARCHAR(255),
  ADD COLUMN IF NOT EXISTS shipping_address_line2 VARCHAR(255),
  ADD COLUMN IF NOT EXISTS shipping_city VARCHAR(120),
  ADD COLUMN IF NOT EXISTS shipping_state VARCHAR(120),
  ADD COLUMN IF NOT EXISTS shipping_postal_code VARCHAR(40),
  ADD COLUMN IF NOT EXISTS shipping_country VARCHAR(2);

