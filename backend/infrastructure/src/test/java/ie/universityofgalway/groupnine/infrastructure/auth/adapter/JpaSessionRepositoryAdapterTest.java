package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.SessionEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.SessionJpaRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaSessionRepositoryAdapterTest {
    @Test
    void delegatesRevokeOperationsToRepository() {
        SessionJpaRepository repo = mock(SessionJpaRepository.class);
        JpaSessionRepositoryAdapter adapter = new JpaSessionRepositoryAdapter(repo);

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        adapter.revokeSession(id, now, "reason", null);
        verify(repo).markRevoked(eq(id), eq(now), eq("reason"), isNull());

        adapter.revokeAllForUser(UserId.newId(), now, "logout_all");
        verify(repo).revokeAllActiveForUser(any(), eq(now), eq("logout_all"));

        adapter.revokeChainFrom(id, now, "reuse");
        verify(repo).revokeChainFrom(eq(id), eq(now), eq("reuse"));
    }

    @Test
    void mapsToDomainOnFind() {
        SessionJpaRepository repo = mock(SessionJpaRepository.class);
        JpaSessionRepositoryAdapter adapter = new JpaSessionRepositoryAdapter(repo);
        SessionEntity e = new SessionEntity();
        UUID id = UUID.randomUUID();
        e.setId(id);
        e.setUserId(UUID.randomUUID());
        e.setRefreshTokenHash("h");
        e.setUserAgent("ua");
        e.setIpAddress("127.0.0.1");
        e.setCreatedAt(Instant.now());
        e.setExpiresAt(Instant.now().plusSeconds(60));
        when(repo.findById(id)).thenReturn(Optional.of(e));
        Optional<Session> s = adapter.findById(id);
        assertTrue(s.isPresent());
        assertEquals(id, s.get().getId());
    }
}

