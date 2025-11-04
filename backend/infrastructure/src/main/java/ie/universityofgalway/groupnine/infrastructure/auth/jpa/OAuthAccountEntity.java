package ie.universityofgalway.groupnine.infrastructure.auth.jpa;

import ie.universityofgalway.groupnine.domain.auth.OAuthProvider;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oauth_accounts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "provider_user_id"}),
                @UniqueConstraint(columnNames = {"provider", "user_id"})
        })
public class OAuthAccountEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Postgres enum oauth_provider
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, columnDefinition = "oauth_provider")
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "email", columnDefinition = "citext")
    private String email;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public OAuthProvider getProvider() { return provider; }
    public void setProvider(OAuthProvider provider) { this.provider = provider; }
    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
