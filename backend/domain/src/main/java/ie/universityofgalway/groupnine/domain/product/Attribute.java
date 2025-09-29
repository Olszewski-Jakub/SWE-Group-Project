package ie.universityofgalway.groupnine.domain.product;

/**
 * Arbitrary name/value attribute associated with a {@link Variant} or {@link Product}.
 * Examples: {@code ("color","Blue")}, {@code ("size","XL")}.
 *
 * @param name  attribute key (e.g., "color")
 * @param value attribute value (e.g., "Blue")
 */
public record Attribute(String name, String value) {}

