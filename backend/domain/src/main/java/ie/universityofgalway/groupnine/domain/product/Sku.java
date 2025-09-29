package ie.universityofgalway.groupnine.domain.product;

/**
 * Stock Keeping Unit (SKU) code used to identify a specific variant in
 * inventory, warehousing, and external systems.
 *
 * @param value SKU string as stored/printed in systems (non-null, non-blank)
 */
public record Sku(String value) {}

