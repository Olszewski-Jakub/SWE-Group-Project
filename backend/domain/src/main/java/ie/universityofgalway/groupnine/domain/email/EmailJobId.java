package ie.universityofgalway.groupnine.domain.email;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique identifier for an email job, used for idempotency and traceability.
 */
public record EmailJobId(UUID value) {
    /**
     * Validates the identifier is non-null.
     */
    public EmailJobId {
        Objects.requireNonNull(value, "id");
    }

    /**
     * Generates a new random job identifier.
     */
    public static EmailJobId newId() {
        return new EmailJobId(UUID.randomUUID());
    }
}
