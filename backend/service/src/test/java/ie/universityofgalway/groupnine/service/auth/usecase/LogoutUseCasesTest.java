package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogoutUseCasesTest {

    @Test
    void logoutRevokesSession() {
        SessionRepositoryPort repo = mock(SessionRepositoryPort.class);
        RandomTokenPort random = mock(RandomTokenPort.class);
        ClockPort clock = mock(ClockPort.class);
        AuditEventPort audit = mock(AuditEventPort.class);
        when(random.sha256("tok")).thenReturn("h");
        Instant now = Instant.now();
        when(clock.now()).thenReturn(now);
        Session s = new Session(UUID.randomUUID(), UserId.newId(), "h", null, null, now.minusSeconds(60), now.plusSeconds(3600), null, null, null);
        when(repo.findByRefreshTokenHash("h")).thenReturn(Optional.of(s));

        new LogoutUseCase(repo, random, audit, clock).execute("tok");
        verify(repo).revokeSession(eq(s.getId()), eq(now), eq("logout"), isNull());
    }

    @Test
    void logoutAllRevokesAllForUser() {
        SessionRepositoryPort repo = mock(SessionRepositoryPort.class);
        RandomTokenPort random = mock(RandomTokenPort.class);
        ClockPort clock = mock(ClockPort.class);
        AuditEventPort audit = mock(AuditEventPort.class);
        when(random.sha256("tok")).thenReturn("h");
        Instant now = Instant.now();
        when(clock.now()).thenReturn(now);
        Session s = new Session(UUID.randomUUID(), UserId.newId(), "h", null, null, now.minusSeconds(60), now.plusSeconds(3600), null, null, null);
        when(repo.findByRefreshTokenHash("h")).thenReturn(Optional.of(s));

        new LogoutAllUseCase(repo, random, audit, clock).execute("tok");
        verify(repo).revokeAllForUser(eq(s.getUserId()), eq(now), eq("logout_all"));
    }
}
