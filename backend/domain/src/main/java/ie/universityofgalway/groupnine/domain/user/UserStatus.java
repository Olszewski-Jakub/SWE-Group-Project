package ie.universityofgalway.groupnine.domain.user;

/**
 * Status of a user account within the system.
 */
public enum UserStatus {
    /**
     * Active and allowed to authenticate.
     */
    ACTIVE,
    /**
     * Temporarily locked (e.g., due to security policy).
     */
    LOCKED,
    /**
     * Soft-deleted or deactivated.
     */
    DELETED
}
