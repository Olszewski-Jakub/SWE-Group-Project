package ie.universityofgalway.groupnine.domain.auth.exception;

/**
 * Thrown when supplied credentials are invalid.
 */
public class InvalidCredentials extends RuntimeException {
    public InvalidCredentials(String message) {
        super(message);
    }
}

