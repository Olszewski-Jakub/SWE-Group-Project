package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.infrastructure.auth.jpa.OAuthAccountEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.OAuthAccountJpaRepository;
import ie.universityofgalway.groupnine.domain.auth.OAuthProvider;
import ie.universityofgalway.groupnine.service.auth.port.OAuthAccountRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JpaOAuthAccountRepositoryAdapter implements OAuthAccountRepositoryPort {

    private final OAuthAccountJpaRepository repo;

    @Autowired
    public JpaOAuthAccountRepositoryAdapter(OAuthAccountJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean isLinked(OAuthProvider provider, UUID userId) {
        return repo.existsByProviderAndUserId(provider.name(), userId);
    }

    @Override
    public boolean existsByProviderUserId(OAuthProvider provider, String providerUserId) {
        return repo.findByProviderAndProviderUserId(provider.name(), providerUserId) != null;
    }

    @Override
    public void link(OAuthProvider provider, String providerUserId, String email, UUID userId) {
        if (isLinked(provider, userId)) return;
        if (existsByProviderUserId(provider, providerUserId)) return;
        OAuthAccountEntity e = new OAuthAccountEntity();
        e.setId(UUID.randomUUID());
        e.setUserId(userId);
        e.setProvider(provider);
        e.setProviderUserId(providerUserId);
        e.setEmail(email);
        e.setCreatedAt(Instant.now());
        repo.save(e);
    }
}
