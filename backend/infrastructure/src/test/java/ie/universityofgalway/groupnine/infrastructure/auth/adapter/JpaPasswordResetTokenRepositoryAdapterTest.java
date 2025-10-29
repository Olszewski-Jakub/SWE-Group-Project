package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.domain.auth.PasswordResetToken;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.PasswordResetTokenEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.PasswordResetTokenJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaPasswordResetTokenRepositoryAdapterTest {

    @Test
    void save_mapsToEntityAndBack() {
        PasswordResetTokenJpaRepository repo = mock(PasswordResetTokenJpaRepository.class);
        JpaPasswordResetTokenRepositoryAdapter adapter = new JpaPasswordResetTokenRepositoryAdapter(repo);
        UUID id = UUID.randomUUID();
        UserId userId = UserId.of(UUID.randomUUID());
        Instant now = Instant.now();
        PasswordResetToken domain = new PasswordResetToken(id, userId, "hash", now.plusSeconds(3600), now, null);

        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        PasswordResetToken saved = adapter.save(domain);

        ArgumentCaptor<PasswordResetTokenEntity> cap = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(repo).save(cap.capture());
        assertEquals(id, cap.getValue().getId());
        assertEquals("hash", cap.getValue().getTokenHash());
        assertEquals(userId.value(), cap.getValue().getUserId());
        assertEquals(id, saved.id());
    }

    @Test
    void invalidateAllForUser_delegates() {
        PasswordResetTokenJpaRepository repo = mock(PasswordResetTokenJpaRepository.class);
        JpaPasswordResetTokenRepositoryAdapter adapter = new JpaPasswordResetTokenRepositoryAdapter(repo);
        UserId uid = UserId.of(UUID.randomUUID());
        Instant t = Instant.now();
        adapter.invalidateAllForUser(uid, t);
        verify(repo).invalidateAllForUser(uid.value(), t);
    }

    @Test
    void findByHash_mapsToDomain() {
        PasswordResetTokenJpaRepository repo = mock(PasswordResetTokenJpaRepository.class);
        JpaPasswordResetTokenRepositoryAdapter adapter = new JpaPasswordResetTokenRepositoryAdapter(repo);
        PasswordResetTokenEntity e = new PasswordResetTokenEntity();
        e.setId(UUID.randomUUID());
        e.setUserId(UUID.randomUUID());
        e.setTokenHash("h");
        e.setExpiresAt(Instant.now());
        e.setCreatedAt(Instant.now());
        when(repo.findByTokenHash("h")).thenReturn(Optional.of(e));
        assertTrue(adapter.findByHash("h").isPresent());
    }

    @Test
    void markUsed_sets_usedAt_and_saves() {
        PasswordResetTokenJpaRepository repo = mock(PasswordResetTokenJpaRepository.class);
        JpaPasswordResetTokenRepositoryAdapter adapter = new JpaPasswordResetTokenRepositoryAdapter(repo);
        UUID id = UUID.randomUUID();
        PasswordResetTokenEntity e = new PasswordResetTokenEntity();
        e.setId(id);
        when(repo.findById(id)).thenReturn(Optional.of(e));
        Instant usedAt = Instant.now();
        adapter.markUsed(id, usedAt);
        assertEquals(usedAt, e.getUsedAt());
        verify(repo).save(e);
    }
}

