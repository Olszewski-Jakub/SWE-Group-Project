package ie.universityofgalway.groupnine.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed wrapper around a UUID user identifier.
 */
public final class UserId {
    private final UUID value;

    private UserId(UUID value) {
        this.value = value;
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    /**
     * Create an identifier instance from an existing UUID.
     */
    public static UserId of(UUID id) {
        if (id == null) throw new IllegalArgumentException("UserId cannot be null");
        return new UserId(id);
    }

    /**
     * Returns the underlying UUID value.
     */
    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return value.equals(userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
