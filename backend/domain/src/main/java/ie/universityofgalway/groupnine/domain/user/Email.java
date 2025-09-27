package ie.universityofgalway.groupnine.domain.user;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email value object with basic format validation and normalization.
 * <p>
 * Normalizes to lowercase and trims whitespace; enforces a basic single-@ format
 * suitable for application-level validation (not exhaustive per RFC).
 */
public final class Email {
    private static final Pattern SIMPLE_EMAIL = Pattern.compile("^[^@\n\r\t ]+@[^@\n\r\t ]+\\.[^@\n\r\t ]+$");

    private final String value;

    private Email(String value) {
        this.value = value.toLowerCase();
    }

    public static Email of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        String trimmed = raw.trim();
        if (!SIMPLE_EMAIL.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return new Email(trimmed);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
