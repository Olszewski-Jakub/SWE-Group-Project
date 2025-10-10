package ie.universityofgalway.groupnine.domain.cart;

import java.util.UUID;

/**
 * Value object representing the unique public identifier of a {@link ShoppingCart}.
 * <p>
 * This is used externally (e.g., in APIs) instead of internal database IDs.
 * It ensures strong typing and prevents ID mix-ups.
 */
public record CartId(UUID id) {

    /**
     * Creates a new {@code CartId} from its string representation.
     *
     * @param id string-form UUID
     * @throws IllegalArgumentException if the string cannot be parsed as a UUID
     */
    public CartId(String id) {
        this(UUID.fromString(id));
    }
}
