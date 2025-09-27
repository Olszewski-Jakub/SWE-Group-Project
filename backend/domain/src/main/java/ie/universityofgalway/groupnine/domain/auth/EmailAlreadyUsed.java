package ie.universityofgalway.groupnine.domain.auth;

/**
 * Thrown when attempting to register with an email address already present in the system.
 */
public class EmailAlreadyUsed extends RuntimeException {
    public EmailAlreadyUsed(String message) {
        super(message);
    }
}
