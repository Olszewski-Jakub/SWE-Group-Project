package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.exception.ExpiredPasswordResetToken;
import ie.universityofgalway.groupnine.domain.auth.exception.InvalidPasswordResetToken;
import ie.universityofgalway.groupnine.domain.auth.PasswordResetToken;
import ie.universityofgalway.groupnine.domain.auth.exception.TokenAlreadyUsed;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordResetTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.util.logging.AppLogger;

import java.time.Instant;
import java.util.Objects;

/**
 * Use case to finalize a password reset using an opaque token and a new password.
 */
public class ResetPasswordUseCase {
    private static final AppLogger log = AppLogger.get(ResetPasswordUseCase.class);

    private final PasswordResetTokenRepositoryPort tokens;
    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final RandomTokenPort randomTokens;
    private final SessionRepositoryPort sessions;
    private final ClockPort clock;

    public ResetPasswordUseCase(PasswordResetTokenRepositoryPort tokens,
                                UserRepositoryPort users,
                                PasswordHasherPort hasher,
                                RandomTokenPort randomTokens,
                                SessionRepositoryPort sessions,
                                ClockPort clock) {
        this.tokens = Objects.requireNonNull(tokens);
        this.users = Objects.requireNonNull(users);
        this.hasher = Objects.requireNonNull(hasher);
        this.randomTokens = Objects.requireNonNull(randomTokens);
        this.sessions = Objects.requireNonNull(sessions);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Resets the password for the user associated with the given token.
     *
     * @param opaqueToken the opaque reset token from the email link
     * @param newPassword the new password to set
     */
    public void execute(String opaqueToken, String newPassword) {
        log.info("reset_password_start");
        if (opaqueToken == null || opaqueToken.isBlank()) {
            throw new InvalidPasswordResetToken("Token is required");
        }
        if (newPassword == null || newPassword.length() < 10) {
            throw new IllegalArgumentException("Password must be at least 10 characters long");
        }

        String hash = randomTokens.sha256(opaqueToken.trim());
        PasswordResetToken token = tokens.findByHash(hash)
                .orElseThrow(() -> new InvalidPasswordResetToken("Invalid password reset token"));

        Instant now = clock.now();
        if (token.isUsed()) {
            log.info("reset_password_token_used", "tokenId", token.id().toString());
            throw new TokenAlreadyUsed("Password reset token already used");
        }
        if (token.isExpiredAt(now)) {
            log.info("reset_password_token_expired", "tokenId", token.id().toString(), "expiredAt", token.expiresAt().toString());
            throw new ExpiredPasswordResetToken("Password reset token expired");
        }

        User user = users.findById(token.userId()).orElseThrow(() -> new InvalidPasswordResetToken("Invalid token user"));

        String newHash = hasher.hash(newPassword);
        User updated = new User(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.isEmailVerified(),
                newHash,
                user.getCreatedAt(),
                now
        );
        users.update(updated);

        tokens.markUsed(token.id(), now);
        tokens.invalidateAllForUser(user.getId(), now);

        sessions.revokeAllForUser(user.getId(), now, "password_reset");
        log.info("reset_password_success", "userId", user.getId().toString());
    }
}

