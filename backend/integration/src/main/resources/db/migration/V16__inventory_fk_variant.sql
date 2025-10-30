-- V16__inventory_fk_variant.sql
-- Add FK from inventory.variant_id to product_variants(uuid)

ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants(uuid)
        ON DELETE CASCADE;

