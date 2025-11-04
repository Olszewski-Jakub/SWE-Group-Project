-- V10__payments_orders_reservations_outbox.sql

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  cart_id UUID NOT NULL,
  total_minor BIGINT NOT NULL,
  currency CHAR(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  snapshot_json JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_cart_id ON orders(cart_id);

-- Inventory reservations
CREATE TABLE IF NOT EXISTS inventory_reservations (
  id UUID PRIMARY KEY,
  order_id UUID NOT NULL,
  items_json JSONB NOT NULL,
  status VARCHAR(32) NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_inventory_reservations_order ON inventory_reservations(order_id);

-- Processed inbound events for idempotency
CREATE TABLE IF NOT EXISTS processed_events (
  source VARCHAR(64) NOT NULL,
  key VARCHAR(255) NOT NULL,
  processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (source, key)
);

-- Transactional Outbox
CREATE TABLE IF NOT EXISTS outbox_messages (
  id UUID PRIMARY KEY,
  exchange VARCHAR(128) NOT NULL,
  routing_key VARCHAR(256) NOT NULL,
  headers_json JSONB,
  payload_json JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  published_at TIMESTAMPTZ,
  attempts INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished ON outbox_messages(published_at) WHERE published_at IS NULL;
