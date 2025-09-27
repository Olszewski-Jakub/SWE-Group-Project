package ie.universityofgalway.groupnine.security.jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Library-agnostic JWT claims representation to avoid leaking vendor classes.
 */
public final class JwtClaims {
    private final String subject;
    private final String issuer;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final List<String> roles;
    private final Map<String, Object> claims;

    public JwtClaims(String subject, String issuer, Instant issuedAt, Instant expiresAt, List<String> roles, Map<String, Object> claims) {
        this.subject = subject;
        this.issuer = issuer;
        this.issuedAt = issuedAt;
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        this.roles = roles == null ? List.of() : List.copyOf(roles);
        this.claims = claims == null ? Map.of() : Map.copyOf(claims);
    }

    public String getSubject() { return subject; }
    public String getIssuer() { return issuer; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public List<String> getRoles() { return roles; }
    public Map<String, Object> getClaims() { return claims; }

    public Object claim(String name) { return claims.get(name); }
}

