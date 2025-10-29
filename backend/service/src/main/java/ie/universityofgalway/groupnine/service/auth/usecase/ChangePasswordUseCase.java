package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.exception.InvalidCredentials;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.util.logging.AppLogger;

import java.time.Instant;
import java.util.Objects;

/**
 * Change the password for an authenticated user.
 * Mirrors password policy and session revocation behavior from ResetPasswordUseCase.
 */
public class ChangePasswordUseCase {
    private static final AppLogger log = AppLogger.get(ChangePasswordUseCase.class);

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final SessionRepositoryPort sessions;
    private final ClockPort clock;

    public ChangePasswordUseCase(UserRepositoryPort users,
                                 PasswordHasherPort hasher,
                                 SessionRepositoryPort sessions,
                                 ClockPort clock) {
        this.users = Objects.requireNonNull(users);
        this.hasher = Objects.requireNonNull(hasher);
        this.sessions = Objects.requireNonNull(sessions);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Change password for the given user.
     *
     * @param userId          authenticated user id
     * @param currentPassword current password to verify
     * @param newPassword     new password (min length 10)
     */
    public void execute(UserId userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 10) {
            throw new IllegalArgumentException("Password must be at least 10 characters long");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new InvalidCredentials("Current password is required");
        }

        User user = users.findById(userId)
                .orElseThrow(() -> new InvalidCredentials("User not found"));

        if (user.getPasswordHash() == null || !hasher.matches(currentPassword, user.getPasswordHash())) {
            log.info("change_password_bad_current", "userId", user.getId().toString());
            throw new InvalidCredentials("Current password is incorrect");
        }

        Instant now = clock.now();
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

        sessions.revokeAllForUser(user.getId(), now, "password_change");
        log.info("change_password_success", "userId", user.getId().toString());
    }
}

