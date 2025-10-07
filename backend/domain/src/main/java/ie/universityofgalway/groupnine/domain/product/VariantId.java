package ie.universityofgalway.groupnine.domain.product;

import java.util.UUID;
/**
 * Value object representing the public identifier of a {@link Variant}.
 * Wraps a UUID to avoid mixing IDs across aggregates.
 */
public record VariantId(UUID id) {}

