package ie.universityofgalway.groupnine.service.auth.port;

import java.util.List;
import java.util.Map;

/**
 * Abstraction over access-token creation to decouple service layer from a specific JWT library.
 */
public interface JwtAccessTokenPort {
    /**
     * Create a signed access token with the given subject and role claims.
     * @param subjectUserId canonical user identifier used as JWT {@code sub}
     * @param roles list of role names to place under the configured roles claim
     * @param extraClaims additional claims to embed (optional)
     * @return serialized access token
     */
    String createAccessToken(String subjectUserId, List<String> roles, Map<String, Object> extraClaims);

    /**
     * Access token time-to-live in seconds.
     */
    long getAccessTokenTtlSeconds();
}
