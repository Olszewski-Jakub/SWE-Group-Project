package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.EmailVerificationTokenEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.EmailVerificationTokenJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaVerificationTokenRepositoryAdapterTest {

    private EmailVerificationTokenJpaRepository repo;
    private JpaVerificationTokenRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repo = mock(EmailVerificationTokenJpaRepository.class);
        adapter = new JpaVerificationTokenRepositoryAdapter(repo);
    }

    @Test
    void save_find_markUsed() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        EmailVerificationToken token = EmailVerificationToken.createNew(UserId.newId(), "hash", now.plusSeconds(3600), now);

        when(repo.save(any(EmailVerificationTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailVerificationToken saved = adapter.save(token);
        assertEquals(token.tokenHash(), saved.tokenHash());

        EmailVerificationTokenEntity entity = new EmailVerificationTokenEntity();
        entity.setId(saved.id());
        entity.setUserId(saved.userId().value());
        entity.setTokenHash(saved.tokenHash());
        entity.setExpiresAt(saved.expiresAt());
        entity.setCreatedAt(saved.createdAt());

        when(repo.findByTokenHash("hash")).thenReturn(Optional.of(entity));
        assertTrue(adapter.findByHash("hash").isPresent());

        when(repo.findById(saved.id())).thenReturn(Optional.of(entity));
        when(repo.save(any(EmailVerificationTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.markUsed(saved.id(), now.plusSeconds(5));
        verify(repo, times(2)).save(any(EmailVerificationTokenEntity.class));
    }
}
