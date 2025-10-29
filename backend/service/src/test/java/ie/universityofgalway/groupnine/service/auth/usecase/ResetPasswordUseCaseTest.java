package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.exception.ExpiredPasswordResetToken;
import ie.universityofgalway.groupnine.domain.auth.exception.InvalidPasswordResetToken;
import ie.universityofgalway.groupnine.domain.auth.PasswordResetToken;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordResetTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ResetPasswordUseCaseTest {
    PasswordResetTokenRepositoryPort tokens = mock(PasswordResetTokenRepositoryPort.class);
    UserRepositoryPort users = mock(UserRepositoryPort.class);
    PasswordHasherPort hasher = mock(PasswordHasherPort.class);
    RandomTokenPort random = mock(RandomTokenPort.class);
    SessionRepositoryPort sessions = mock(SessionRepositoryPort.class);
    ClockPort clock = mock(ClockPort.class);

    ResetPasswordUseCase uc;
    UserId userId;
    User user;
    Instant now;

    @BeforeEach
    void setup() {
        uc = new ResetPasswordUseCase(tokens, users, hasher, random, sessions, clock);
        userId = UserId.newId();
        now = Instant.parse("2024-01-02T00:00:00Z");
        user = new User(userId, Email.of("jane@example.com"), "Jane", "Doe", UserStatus.ACTIVE, true, "old-hash", Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"));
        when(clock.now()).thenReturn(now);
        when(hasher.hash(eq("new-password-123"))).thenReturn("new-hash");
        when(users.findById(eq(userId))).thenReturn(Optional.of(user));
        when(users.update(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(random.sha256(eq("opaque"))).thenReturn("hash");
    }

    @Test
    void resets_password_and_revokes_sessions() {
        PasswordResetToken token = PasswordResetToken.createNew(userId, "hash", now.plusSeconds(60), now.minusSeconds(60));
        when(tokens.findByHash(eq("hash"))).thenReturn(Optional.of(token));

        uc.execute("opaque", "new-password-123");

        verify(users).update(any(User.class));
        verify(tokens).markUsed(eq(token.id()), eq(now));
        verify(tokens).invalidateAllForUser(eq(userId), eq(now));
        verify(sessions).revokeAllForUser(eq(userId), eq(now), eq("password_reset"));
    }

    @Test
    void invalid_token_throws() {
        when(tokens.findByHash(eq("hash"))).thenReturn(Optional.empty());
        assertThrows(InvalidPasswordResetToken.class, () -> uc.execute("opaque", "new-password-123"));
    }

    @Test
    void expired_token_throws() {
        PasswordResetToken token = PasswordResetToken.createNew(userId, "hash", now.minusSeconds(1), now.minusSeconds(120));
        when(tokens.findByHash(eq("hash"))).thenReturn(Optional.of(token));
        assertThrows(ExpiredPasswordResetToken.class, () -> uc.execute("opaque", "new-password-123"));
    }
}

