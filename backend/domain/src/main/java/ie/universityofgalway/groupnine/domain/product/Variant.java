package ie.universityofgalway.groupnine.domain.product;

import java.util.List;
/**
 * Domain model for a concrete, sellable variation of a {@link Product}
 * (e.g., size, color, configuration).
 */
public record Variant(
    VariantId id,
    Sku sku,
    Money price,
    Stock stock,
    List<Attribute> attributes
) {}
