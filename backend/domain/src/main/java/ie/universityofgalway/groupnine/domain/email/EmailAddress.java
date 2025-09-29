package ie.universityofgalway.groupnine.domain.email;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Strongly-typed email address value object with minimal validation.
 */
public record EmailAddress(String value) {
    private static final Pattern SIMPLE = Pattern.compile("^[^@\n\r]+@[^@\n\r]+\\.[^@\n\r]+$");

    /**
     * Validates and normalizes the email address.
     * Trims whitespace and applies a conservative pattern check.
     *
     * @throws IllegalArgumentException if the value is null, blank, or malformed
     */
    public EmailAddress {
        Objects.requireNonNull(value, "email value");
        String v = value.trim();
        if (v.isEmpty() || !SIMPLE.matcher(v).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        value = v;
    }
}
