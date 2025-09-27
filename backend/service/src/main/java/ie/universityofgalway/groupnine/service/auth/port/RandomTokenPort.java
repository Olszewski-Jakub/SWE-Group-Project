package ie.universityofgalway.groupnine.service.auth.port;

/**
 * Port for generating secure random tokens and hashing them for storage.
 */
public interface RandomTokenPort {
    /**
     * Generate an opaque random token (base64url) using at least 32 random bytes.
     */
    String generateOpaqueToken();

    /**
     * Compute SHA-256 of the given token and return a base64url-encoded string.
     */
    String sha256(String token);
}
