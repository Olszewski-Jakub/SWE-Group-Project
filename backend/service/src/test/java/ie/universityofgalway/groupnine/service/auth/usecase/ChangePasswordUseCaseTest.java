package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.exception.InvalidCredentials;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
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

class ChangePasswordUseCaseTest {
    UserRepositoryPort users = mock(UserRepositoryPort.class);
    PasswordHasherPort hasher = mock(PasswordHasherPort.class);
    SessionRepositoryPort sessions = mock(SessionRepositoryPort.class);
    ClockPort clock = mock(ClockPort.class);

    ChangePasswordUseCase uc;
    UserId userId;
    User user;

    @BeforeEach
    void setup() {
        uc = new ChangePasswordUseCase(users, hasher, sessions, clock);
        userId = UserId.newId();
        user = new User(userId, Email.of("jane@example.com"), "Jane", "Doe", UserStatus.ACTIVE, true, "hash", Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"));
        when(users.findById(eq(userId))).thenReturn(Optional.of(user));
        when(clock.now()).thenReturn(Instant.parse("2024-01-02T00:00:00Z"));
    }

    @Test
    void changes_password_and_revokes_sessions_on_success() {
        when(hasher.matches(eq("current-pass"), eq("hash"))).thenReturn(true);
        when(hasher.hash(eq("new-password-123"))).thenReturn("new-hash");

        // emulate update: return argument passed
        when(users.update(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        uc.execute(userId, "current-pass", "new-password-123");

        verify(users).update(any(User.class));
        verify(sessions).revokeAllForUser(eq(userId), any(), eq("password_change"));
    }

    @Test
    void rejects_short_new_password() {
        assertThrows(IllegalArgumentException.class, () -> uc.execute(userId, "curr", "short"));
    }

    @Test
    void rejects_missing_current_password() {
        assertThrows(InvalidCredentials.class, () -> uc.execute(userId, "", "new-password-123"));
    }

    @Test
    void rejects_wrong_current_password() {
        when(hasher.matches(eq("wrong"), any())).thenReturn(false);
        assertThrows(InvalidCredentials.class, () -> uc.execute(userId, "wrong", "new-password-123"));
    }
}

