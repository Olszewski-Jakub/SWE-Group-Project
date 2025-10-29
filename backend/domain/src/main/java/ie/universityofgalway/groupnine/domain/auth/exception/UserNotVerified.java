package ie.universityofgalway.groupnine.domain.auth.exception;

/**
 * Thrown when a user attempts to login without a verified email.
 */
public class UserNotVerified extends RuntimeException {
    public UserNotVerified(String message) {
        super(message);
    }
}

