package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.InvalidCredentials;
import ie.universityofgalway.groupnine.domain.auth.TooManyAttempts;
import ie.universityofgalway.groupnine.domain.auth.UserLocked;
import ie.universityofgalway.groupnine.domain.auth.UserNotVerified;
import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.factory.RefreshTokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.BruteForceGuardPort;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.JwtAccessTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LoginUseCaseTest {

    @Test
    void loginSuccessReturnsTokensAndCreatesSession() throws Exception {
        UserRepositoryPort users = mock(UserRepositoryPort.class);
        PasswordHasherPort hasher = mock(PasswordHasherPort.class);
        SessionRepositoryPort sessions = mock(SessionRepositoryPort.class);
        JwtAccessTokenPort jwt = mock(JwtAccessTokenPort.class);
        BruteForceGuardPort guard = mock(BruteForceGuardPort.class);
        RefreshTokenFactory factory = mock(RefreshTokenFactory.class);
        AuditEventPort audit = mock(AuditEventPort.class);
        ClockPort clock = mock(ClockPort.class);

        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);
        when(guard.check(any(), any())).thenReturn(new BruteForceGuardPort.Verdict(true, null));
        when(factory.generate()).thenReturn(new RefreshTokenFactory.Pair("refTok", "refHash"));
        when(jwt.createAccessToken(anyString(), anyList(), any())).thenReturn("acc");
        when(jwt.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(hasher.matches(eq("pass"), anyString())).thenReturn(true);

        User user = new User(
                UserId.newId(),
                Email.of("user@example.com"),
                "fn", "ln",
                UserStatus.ACTIVE,
                true,
                "hash",
                now, now
        );
        when(users.findByEmail(Email.of("user@example.com"))).thenReturn(Optional.of(user));
        when(sessions.save(any())).thenAnswer(inv -> {
            Session s = inv.getArgument(0);
            return new Session(UUID.randomUUID(), s.getUserId(), s.getRefreshTokenHash(), s.getUserAgent(), s.getIpAddress(), s.getCreatedAt(), s.getExpiresAt(), s.getRevokedAt(), s.getReplacedBySessionId(), s.getReason());
        });

        LoginUseCase uc = new LoginUseCase(users, hasher, sessions, jwt, guard, factory, audit, clock, Duration.ofDays(14));
        LoginUseCase.Result result = uc.execute("user@example.com", "pass", "UA", InetAddress.getByName("127.0.0.1"));

        assertEquals("acc", result.accessToken());
        assertEquals("refTok", result.refreshToken());
        verify(sessions, times(1)).save(any());
        verify(guard).recordSuccess(any(), any());
    }

    @Test
    void rateLimitedThrowsTooManyAttemptsWithRetryAfter() {
        UserRepositoryPort users = mock(UserRepositoryPort.class);
        PasswordHasherPort hasher = mock(PasswordHasherPort.class);
        SessionRepositoryPort sessions = mock(SessionRepositoryPort.class);
        JwtAccessTokenPort jwt = mock(JwtAccessTokenPort.class);
        BruteForceGuardPort guard = mock(BruteForceGuardPort.class);
        RefreshTokenFactory factory = mock(RefreshTokenFactory.class);
        AuditEventPort audit = mock(AuditEventPort.class);
        ClockPort clock = mock(ClockPort.class);

        when(guard.check(any(), any())).thenReturn(new BruteForceGuardPort.Verdict(false, 120L));

        LoginUseCase uc = new LoginUseCase(users, hasher, sessions, jwt, guard, factory, audit, clock, Duration.ofDays(14));
        TooManyAttempts ex = assertThrows(TooManyAttempts.class, () -> uc.execute("user@example.com", "pass", null, null));
        assertTrue(ex.getMessage().contains("Retry after 120 seconds"));
        verifyNoInteractions(users, hasher, sessions, jwt);
    }

    @Test
    void badPasswordThrowsInvalidCredentials() {
        UserRepositoryPort users = mock(UserRepositoryPort.class);
        PasswordHasherPort hasher = mock(PasswordHasherPort.class);
        SessionRepositoryPort sessions = mock(SessionRepositoryPort.class);
        JwtAccessTokenPort jwt = mock(JwtAccessTokenPort.class);
        BruteForceGuardPort guard = mock(BruteForceGuardPort.class);
        RefreshTokenFactory factory = mock(RefreshTokenFactory.class);
        AuditEventPort audit = mock(AuditEventPort.class);
        ClockPort clock = mock(ClockPort.class);

        when(guard.check(any(), any())).thenReturn(new BruteForceGuardPort.Verdict(true, null));
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);

        User user = new User(UserId.newId(), Email.of("user@example.com"), "fn", "ln", UserStatus.ACTIVE, true, "hash", now, now);
        when(users.findByEmail(Email.of("user@example.com"))).thenReturn(Optional.of(user));
        when(hasher.matches(eq("bad"), anyString())).thenReturn(false);

        LoginUseCase uc = new LoginUseCase(users, hasher, sessions, jwt, guard, factory, audit, clock, Duration.ofDays(14));
        assertThrows(InvalidCredentials.class, () -> uc.execute("user@example.com", "bad", null, null));
        verify(guard).recordFailure(any(), any());
        verifyNoInteractions(sessions, jwt);
    }

    @Test
    void lockedUserThrowsUserLocked() {
        UserRepositoryPort users = mock(UserRepositoryPort.class);
        PasswordHasherPort hasher = mock(PasswordHasherPort.class);
        SessionRepositoryPort sessions = mock(SessionRepositoryPort.class);
        JwtAccessTokenPort jwt = mock(JwtAccessTokenPort.class);
        BruteForceGuardPort guard = mock(BruteForceGuardPort.class);
        RefreshTokenFactory factory = mock(RefreshTokenFactory.class);
        AuditEventPort audit = mock(AuditEventPort.class);
        ClockPort clock = mock(ClockPort.class);

        when(guard.check(any(), any())).thenReturn(new BruteForceGuardPort.Verdict(true, null));
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);

        User user = new User(UserId.newId(), Email.of("user@example.com"), "fn", "ln", UserStatus.LOCKED, true, "hash", now, now);
        when(users.findByEmail(Email.of("user@example.com"))).thenReturn(Optional.of(user));

        LoginUseCase uc = new LoginUseCase(users, hasher, sessions, jwt, guard, factory, audit, clock, Duration.ofDays(14));
        assertThrows(UserLocked.class, () -> uc.execute("user@example.com", "any", null, null));
        verifyNoInteractions(sessions, jwt);
    }

    @Test
    void notVerifiedThrowsUserNotVerified() {
        UserRepositoryPort users = mock(UserRepositoryPort.class);
        PasswordHasherPort hasher = mock(PasswordHasherPort.class);
        SessionRepositoryPort sessions = mock(SessionRepositoryPort.class);
        JwtAccessTokenPort jwt = mock(JwtAccessTokenPort.class);
        BruteForceGuardPort guard = mock(BruteForceGuardPort.class);
        RefreshTokenFactory factory = mock(RefreshTokenFactory.class);
        AuditEventPort audit = mock(AuditEventPort.class);
        ClockPort clock = mock(ClockPort.class);

        when(guard.check(any(), any())).thenReturn(new BruteForceGuardPort.Verdict(true, null));
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);

        User user = new User(UserId.newId(), Email.of("user@example.com"), "fn", "ln", UserStatus.ACTIVE, false, "hash", now, now);
        when(users.findByEmail(Email.of("user@example.com"))).thenReturn(Optional.of(user));

        LoginUseCase uc = new LoginUseCase(users, hasher, sessions, jwt, guard, factory, audit, clock, Duration.ofDays(14));
        assertThrows(UserNotVerified.class, () -> uc.execute("user@example.com", "any", null, null));
        verifyNoInteractions(sessions, jwt);
    }
}
