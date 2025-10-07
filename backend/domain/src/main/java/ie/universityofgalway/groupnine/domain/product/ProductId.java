package ie.universityofgalway.groupnine.domain.product;

import java.util.UUID;

/**
 * Value object representing the public identifier of a {@link Product}.
 */
public record ProductId(UUID id) {
    /**
     * Convenience constructor to parse a UUID from its string form.
     *
     * @param id UUID string (e.g., "550e8400-e29b-41d4-a716-446655440000")
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public ProductId(String id) {
        this(UUID.fromString(id));
    }

    public UUID getId() {
        return id;
    }
}