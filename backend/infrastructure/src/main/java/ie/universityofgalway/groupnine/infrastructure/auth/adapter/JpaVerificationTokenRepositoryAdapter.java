package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.EmailVerificationTokenEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.EmailVerificationTokenJpaRepository;
import ie.universityofgalway.groupnine.service.auth.port.VerificationTokenRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaVerificationTokenRepositoryAdapter implements VerificationTokenRepositoryPort {

    private final EmailVerificationTokenJpaRepository repo;

    @Autowired
    public JpaVerificationTokenRepositoryAdapter(EmailVerificationTokenJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        EmailVerificationTokenEntity e = toEntity(token);
        EmailVerificationTokenEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<EmailVerificationToken> findByHash(String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void markUsed(UUID tokenId, Instant usedAt) {
        EmailVerificationTokenEntity e = repo.findById(tokenId).orElseThrow();
        e.setUsedAt(usedAt);
        repo.save(e);
    }

    private EmailVerificationTokenEntity toEntity(EmailVerificationToken t) {
        EmailVerificationTokenEntity e = new EmailVerificationTokenEntity();
        e.setId(t.id());
        e.setUserId(t.userId().value());
        e.setTokenHash(t.tokenHash());
        e.setExpiresAt(t.expiresAt());
        e.setCreatedAt(t.createdAt());
        e.setUsedAt(t.usedAt());
        return e;
    }

    private EmailVerificationToken toDomain(EmailVerificationTokenEntity e) {
        return new EmailVerificationToken(
                e.getId(),
                UserId.of(e.getUserId()),
                e.getTokenHash(),
                e.getExpiresAt(),
                e.getCreatedAt(),
                e.getUsedAt()
        );
    }
}
