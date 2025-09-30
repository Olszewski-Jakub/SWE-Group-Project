package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.factory.TokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.EmailSenderPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.VerificationTokenRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterUserUseCaseTest {

    UserRepositoryPort userRepository = mock(UserRepositoryPort.class);
    VerificationTokenRepositoryPort tokenRepository = mock(VerificationTokenRepositoryPort.class);
    PasswordHasherPort passwordHasher = mock(PasswordHasherPort.class);
    ClockPort clock = mock(ClockPort.class);
    EmailSenderPort emailSender = mock(EmailSenderPort.class);
    TokenFactory tokenFactory = mock(TokenFactory.class);

    RegisterUserUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new RegisterUserUseCase(userRepository, tokenRepository, passwordHasher, clock, emailSender, tokenFactory, "http://localhost:8080");
    }

    @Test
    void happy_path_registers_and_sends_email() {
        when(clock.now()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
        when(passwordHasher.hash("supersecurepwd")).thenReturn("bcrypt-hash");
        when(userRepository.existsByEmail(Email.of("john@example.com"))).thenReturn(false);

        // emulate save returning persisted user (id same as constructed)
        // we capture argument and return it back
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailVerificationToken token = EmailVerificationToken.createNew(UserId.newId(), "hash", Instant.parse("2024-01-02T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"));
        when(tokenRepository.save(any())).thenReturn(token);
        when(tokenFactory.createEmailVerification(any(), any(), any())).thenReturn(new TokenFactory.VerificationTokenWithOpaque(token, "opaqueToken"));

        useCase.execute("john@example.com", "supersecurepwd", "John", "Doe");

        ArgumentCaptor<String> urlCap = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendVerificationEmail(eq(Email.of("john@example.com")), urlCap.capture());
        String sentUrl = urlCap.getValue();
        assertTrue(sentUrl.startsWith("http://localhost:8080/verify?token=opaqueToken"));
    }

    @Test
    void sets_token_expiry_24h_after_now() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);
        when(passwordHasher.hash(anyString())).thenReturn("h");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Instant> createdCap = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> expiresCap = ArgumentCaptor.forClass(Instant.class);

        when(tokenFactory.createEmailVerification(any(), createdCap.capture(), expiresCap.capture()))
                .thenReturn(new TokenFactory.VerificationTokenWithOpaque(
                        EmailVerificationToken.createNew(UserId.newId(), "h", now.plusSeconds(86400), now),
                        "opaque"
                ));

        useCase.execute("x@y.com", "0123456789X", "A", "B");

        assertEquals(now, createdCap.getValue());
        assertEquals(now.plusSeconds(86400), expiresCap.getValue());
    }

    @Test
    void invalid_email_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class, () ->
                useCase.execute("not-an-email", "0123456789X", "A", "B")
        );
    }

    @Test
    void saved_user_has_expected_fields() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);
        when(passwordHasher.hash("supersecurepwd")).thenReturn("bcrypt-hash");
        when(userRepository.existsByEmail(any())).thenReturn(false);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        EmailVerificationToken token = EmailVerificationToken.createNew(UserId.newId(), "h", now.plusSeconds(86400), now);
        when(tokenFactory.createEmailVerification(any(), any(), any()))
                .thenReturn(new TokenFactory.VerificationTokenWithOpaque(token, "opaque"));

        useCase.execute("alice@example.com", "supersecurepwd", "Alice", "Doe");

        User created = userCaptor.getValue();
        assertEquals("alice@example.com", created.getEmail().value());
        assertFalse(created.isEmailVerified());
        assertEquals("bcrypt-hash", created.getPasswordHash());
        assertEquals(now, created.getCreatedAt());
    }

    @Test
    void rejects_short_password() {
        assertThrows(IllegalArgumentException.class, () ->
                useCase.execute("a@b.com", "short", "A", "B")
        );
    }

    @Test
    void duplicate_email_throws_conflict() {
        when(userRepository.existsByEmail(Email.of("dup@example.com"))).thenReturn(true);
        assertThrows(ie.universityofgalway.groupnine.domain.auth.EmailAlreadyUsed.class, () ->
                useCase.execute("dup@example.com", "supersecurepwd", "A", "B")
        );
    }
}
