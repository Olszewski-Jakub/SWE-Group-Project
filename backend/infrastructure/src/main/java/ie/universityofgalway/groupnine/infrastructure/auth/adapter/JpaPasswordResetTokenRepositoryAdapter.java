package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.domain.auth.PasswordResetToken;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.PasswordResetTokenEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.PasswordResetTokenJpaRepository;
import ie.universityofgalway.groupnine.service.auth.port.PasswordResetTokenRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaPasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository repo;

    @Autowired
    public JpaPasswordResetTokenRepositoryAdapter(PasswordResetTokenJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity e = toEntity(token);
        PasswordResetTokenEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void invalidateAllForUser(UserId userId, Instant when) {
        repo.invalidateAllForUser(userId.value(), when);
    }

    @Override
    public Optional<PasswordResetToken> findByHash(String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void markUsed(UUID tokenId, Instant usedAt) {
        PasswordResetTokenEntity e = repo.findById(tokenId).orElseThrow();
        e.setUsedAt(usedAt);
        repo.save(e);
    }

    private PasswordResetTokenEntity toEntity(PasswordResetToken t) {
        PasswordResetTokenEntity e = new PasswordResetTokenEntity();
        e.setId(t.id());
        e.setUserId(t.userId().value());
        e.setTokenHash(t.tokenHash());
        e.setExpiresAt(t.expiresAt());
        e.setCreatedAt(t.createdAt());
        e.setUsedAt(t.usedAt());
        return e;
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity e) {
        return new PasswordResetToken(
                e.getId(),
                UserId.of(e.getUserId()),
                e.getTokenHash(),
                e.getExpiresAt(),
                e.getCreatedAt(),
                e.getUsedAt()
        );
    }
}
