package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.ExpiredVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.InvalidVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.TokenAlreadyUsed;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.VerificationTokenRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerifyEmailUseCaseTest {

    VerificationTokenRepositoryPort tokenRepo = mock(VerificationTokenRepositoryPort.class);
    UserRepositoryPort userRepo = mock(UserRepositoryPort.class);
    RandomTokenPort random = mock(RandomTokenPort.class);
    ClockPort clock = mock(ClockPort.class);

    VerifyEmailUseCase useCase = new VerifyEmailUseCase(tokenRepo, userRepo, random, clock);

    @Test
    void happy_path_marks_user_and_token_used() {
        when(random.sha256("opaque")).thenReturn("hash");
        when(clock.now()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));

        UserId uid = UserId.newId();
        EmailVerificationToken token = new EmailVerificationToken(UUID.randomUUID(), uid, "hash",
                Instant.parse("2024-02-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"), null);
        when(tokenRepo.findByHash("hash")).thenReturn(Optional.of(token));

        User user = new User(uid, Email.of("a@b.com"), "A", "B", UserStatus.ACTIVE, false, "h", Instant.now(), Instant.now());
        when(userRepo.findById(uid)).thenReturn(Optional.of(user));

        useCase.execute("opaque");

        verify(userRepo).update(argThat(u -> u.isEmailVerified()));
        verify(tokenRepo).markUsed(eq(token.id()), any(Instant.class));
    }

    @Test
    void invalid_token_throws() {
        when(random.sha256(anyString())).thenReturn("hash");
        when(tokenRepo.findByHash("hash")).thenReturn(Optional.empty());
        assertThrows(InvalidVerificationToken.class, () -> useCase.execute("opaque"));
    }

    @Test
    void expired_token_throws() {
        when(random.sha256("opaque")).thenReturn("hash");
        when(clock.now()).thenReturn(Instant.parse("2024-02-02T00:00:00Z"));
        EmailVerificationToken token = new EmailVerificationToken(UUID.randomUUID(), UserId.newId(), "hash",
                Instant.parse("2024-02-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"), null);
        when(tokenRepo.findByHash("hash")).thenReturn(Optional.of(token));
        assertThrows(ExpiredVerificationToken.class, () -> useCase.execute("opaque"));
    }

    @Test
    void used_token_throws() {
        when(random.sha256("opaque")).thenReturn("hash");
        when(clock.now()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
        EmailVerificationToken token = new EmailVerificationToken(UUID.randomUUID(), UserId.newId(), "hash",
                Instant.parse("2024-02-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:01Z"));
        when(tokenRepo.findByHash("hash")).thenReturn(Optional.of(token));
        assertThrows(TokenAlreadyUsed.class, () -> useCase.execute("opaque"));
    }

    @Test
    void blank_token_throws_invalid() {
        assertThrows(InvalidVerificationToken.class, () -> useCase.execute(" "));
    }

    @Test
    void user_missing_for_valid_token_throws_invalid() {
        when(random.sha256("opaque")).thenReturn("hash");
        when(clock.now()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
        EmailVerificationToken token = new EmailVerificationToken(UUID.randomUUID(), UserId.newId(), "hash",
                Instant.parse("2024-02-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"), null);
        when(tokenRepo.findByHash("hash")).thenReturn(Optional.of(token));
        when(userRepo.findById(token.userId())).thenReturn(Optional.empty());
        assertThrows(InvalidVerificationToken.class, () -> useCase.execute("opaque"));
    }
}
