package ie.universityofgalway.groupnine.domain.auth;

/**
 * Thrown when a user account is locked.
 */
public class UserLocked extends RuntimeException {
    public UserLocked(String message) {
        super(message);
    }
}

