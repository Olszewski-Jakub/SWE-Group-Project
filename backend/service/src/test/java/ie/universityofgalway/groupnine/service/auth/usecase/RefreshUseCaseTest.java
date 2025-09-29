package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.factory.RefreshTokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.JwtAccessTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RefreshUseCaseTest {

    @Test
    void rotatesRefreshToken() throws Exception {
        SessionRepositoryPort repo = mock(SessionRepositoryPort.class);
        JwtAccessTokenPort jwt = mock(JwtAccessTokenPort.class);
        RefreshTokenFactory factory = mock(RefreshTokenFactory.class);
        RandomTokenPort random = mock(RandomTokenPort.class);
        ClockPort clock = mock(ClockPort.class);
        AuditEventPort audit = mock(AuditEventPort.class);

        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);
        when(jwt.createAccessToken(any(), anyList(), any())).thenReturn("acc");
        when(jwt.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(random.sha256("old"))
                .thenReturn("old_hash");

        Session sess = new Session(UUID.randomUUID(), UserId.newId(), "old_hash", "ua", null, now.minusSeconds(10), now.plusSeconds(3600), null, null, null);
        when(repo.findByRefreshTokenHash("old_hash")).thenReturn(Optional.of(sess));
        when(factory.generate()).thenReturn(new RefreshTokenFactory.Pair("new", "new_hash"));
        when(repo.save(any())).thenAnswer(inv -> {
            Session s = inv.getArgument(0);
            return new Session(UUID.randomUUID(), s.getUserId(), s.getRefreshTokenHash(), s.getUserAgent(), s.getIpAddress(), s.getCreatedAt(), s.getExpiresAt(), s.getRevokedAt(), s.getReplacedBySessionId(), s.getReason());
        });

        RefreshUseCase uc = new RefreshUseCase(repo, jwt, factory, random, audit, clock, Duration.ofDays(14));
        RefreshUseCase.Result result = uc.execute("old", "ua", InetAddress.getByName("127.0.0.1"));

        assertEquals("new", result.refreshToken());
        verify(repo).revokeSession(eq(sess.getId()), eq(now), eq("rotated"), any());
    }

    @Test
    void reuseDetectionRevokesAll() {
        SessionRepositoryPort repo = mock(SessionRepositoryPort.class);
        JwtAccessTokenPort jwt = mock(JwtAccessTokenPort.class);
        RefreshTokenFactory factory = mock(RefreshTokenFactory.class);
        RandomTokenPort random = mock(RandomTokenPort.class);
        ClockPort clock = mock(ClockPort.class);
        AuditEventPort audit = mock(AuditEventPort.class);

        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);
        when(random.sha256("stale"))
                .thenReturn("stale_hash");
        Session sess = new Session(UUID.randomUUID(), UserId.newId(), "stale_hash", "ua", null, now.minusSeconds(10), now.plusSeconds(3600), now.minusSeconds(5), UUID.randomUUID(), "rotated");
        when(repo.findByRefreshTokenHash("stale_hash")).thenReturn(Optional.of(sess));

        RefreshUseCase uc = new RefreshUseCase(repo, jwt, factory, random, audit, clock, Duration.ofDays(14));
        assertThrows(
                ie.universityofgalway.groupnine.domain.auth.RefreshReuseDetected.class,
                () -> uc.execute("stale", "ua", null)
        );
        verify(repo, never()).revokeAllForUser(any(), any(), any());
        verify(repo).revokeChainFrom(eq(sess.getId()), eq(now), eq("refresh_token_reuse_detected_chain_revoked"));
    }

    @Test
    void invalidRefreshTokenThrows() {
        SessionRepositoryPort repo = mock(SessionRepositoryPort.class);
        JwtAccessTokenPort jwt = mock(JwtAccessTokenPort.class);
        RefreshTokenFactory factory = mock(RefreshTokenFactory.class);
        RandomTokenPort random = mock(RandomTokenPort.class);
        ClockPort clock = mock(ClockPort.class);
        AuditEventPort audit = mock(AuditEventPort.class);

        when(random.sha256("bad")).thenReturn("bad_hash");
        when(repo.findByRefreshTokenHash("bad_hash")).thenReturn(java.util.Optional.empty());

        RefreshUseCase uc = new RefreshUseCase(repo, jwt, factory, random, audit, clock, Duration.ofDays(14));
        assertThrows(ie.universityofgalway.groupnine.domain.auth.InvalidRefreshToken.class, () -> uc.execute("bad", null, null));
    }

    @Test
    void expiredRefreshTokenThrows() {
        SessionRepositoryPort repo = mock(SessionRepositoryPort.class);
        JwtAccessTokenPort jwt = mock(JwtAccessTokenPort.class);
        RefreshTokenFactory factory = mock(RefreshTokenFactory.class);
        RandomTokenPort random = mock(RandomTokenPort.class);
        ClockPort clock = mock(ClockPort.class);
        AuditEventPort audit = mock(AuditEventPort.class);

        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);
        when(random.sha256("x")).thenReturn("h");
        Session sess = new Session(UUID.randomUUID(), UserId.newId(), "h", "ua", null, now.minusSeconds(3600), now.minusSeconds(1), null, null, null);
        when(repo.findByRefreshTokenHash("h")).thenReturn(java.util.Optional.of(sess));

        RefreshUseCase uc = new RefreshUseCase(repo, jwt, factory, random, audit, clock, Duration.ofDays(14));
        assertThrows(ie.universityofgalway.groupnine.domain.auth.ExpiredRefreshToken.class, () -> uc.execute("x", null, null));
    }
}
