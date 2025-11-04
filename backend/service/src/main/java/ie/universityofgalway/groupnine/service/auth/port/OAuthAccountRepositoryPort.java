package ie.universityofgalway.groupnine.service.auth.port;

import ie.universityofgalway.groupnine.domain.auth.OAuthProvider;

import java.util.UUID;

/**
 * Port for persisting and querying OAuth account linkages to local users.
 */
public interface OAuthAccountRepositoryPort {
    boolean isLinked(OAuthProvider provider, UUID userId);
    boolean existsByProviderUserId(OAuthProvider provider, String providerUserId);
    void link(OAuthProvider provider, String providerUserId, String email, UUID userId);
}
