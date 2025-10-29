-- Add product status column if missing
ALTER TABLE products ADD COLUMN IF NOT EXISTS status VARCHAR(16) NOT NULL DEFAULT 'DRAFT';
UPDATE products SET status = 'DRAFT' WHERE status IS NULL;

