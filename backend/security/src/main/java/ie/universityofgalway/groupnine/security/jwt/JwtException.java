package ie.universityofgalway.groupnine.security.jwt;

/**
 * Unchecked exception representing JWT creation/validation errors.
 */
public class JwtException extends RuntimeException {
    public JwtException(String message) { super(message); }
    public JwtException(String message, Throwable cause) { super(message, cause); }
}
