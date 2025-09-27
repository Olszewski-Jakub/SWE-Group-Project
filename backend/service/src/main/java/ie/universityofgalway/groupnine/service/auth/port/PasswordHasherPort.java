package ie.universityofgalway.groupnine.service.auth.port;

/**
 * Port for hashing and verifying passwords.
 */
public interface PasswordHasherPort {
    /**
     * Hash a raw password with a strong, salted and adaptive algorithm (e.g., BCrypt).
     */
    String hash(String rawPassword);

    /**
     * Verify a raw password against a previously stored hash.
     */
    boolean matches(String rawPassword, String hash);
}
