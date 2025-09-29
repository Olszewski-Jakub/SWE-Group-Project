package ie.universityofgalway.groupnine.domain.email;

/**
 * Enumerates supported email types. Used for routing, template resolution and dispatch.
 */
public enum EmailType {
    /**
     * Verification email containing a confirmation link.
     */
    ACCOUNT_VERIFICATION,
    /**
     * Post-verification welcome message.
     */
    WELCOME
}
