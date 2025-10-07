package ie.universityofgalway.groupnine.domain.product;

import java.time.Instant;
import java.util.List;

/**
 * Aggregate root for products listed in the catalog.
 *
 * @param id          public identifier of the product
 * @param name        product name (human-friendly)
 * @param description optional long-form description
 * @param category    category label for browsing/filters
 * @param status      lifecycle status (draft/active/archived)
 * @param variants    list of available variants (may be empty)
 * @param createdAt   creation timestamp (UTC)
 * @param updatedAt   last update timestamp (UTC)
 *
 */
public record Product(
    ProductId id,
    String name,
    String description,
    String category,
    ProductStatus status,
    List<Variant> variants,
    Instant createdAt,
    Instant updatedAt
) {}