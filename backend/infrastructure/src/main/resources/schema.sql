CREATE TABLE products (
  id           BIGSERIAL PRIMARY KEY,
  name         VARCHAR(120) NOT NULL,
  description  TEXT,
  price_cents  INTEGER NOT NULL CHECK (price_cents >= 0),
  image_url    VARCHAR(255),
  category     VARCHAR(60) NOT NULL,
  is_available BOOLEAN NOT NULL DEFAULT TRUE,
  created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_available ON products(is_available);
