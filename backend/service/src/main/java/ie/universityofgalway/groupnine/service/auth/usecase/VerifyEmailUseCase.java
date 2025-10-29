package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.exception.ExpiredVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.exception.InvalidVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.exception.TokenAlreadyUsed;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.jobs.WelcomeEmailJob;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.VerificationTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort;
import ie.universityofgalway.groupnine.util.logging.AppLogger;

import java.time.Instant;
import java.util.Objects;

/**
 * Use case for confirming a user's email address using a verification token.
 */
public class VerifyEmailUseCase {
    private static final AppLogger log = AppLogger.get(VerifyEmailUseCase.class);
    private final VerificationTokenRepositoryPort tokenRepository;
    private final UserRepositoryPort userRepository;
    private final RandomTokenPort randomTokenPort;
    private final ClockPort clock;
    private final EnqueueEmailPort enqueueEmail;

    public VerifyEmailUseCase(VerificationTokenRepositoryPort tokenRepository,
                              UserRepositoryPort userRepository,
                              RandomTokenPort randomTokenPort,
                              ClockPort clock,
                              EnqueueEmailPort enqueueEmail) {
        this.tokenRepository = Objects.requireNonNull(tokenRepository);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.randomTokenPort = Objects.requireNonNull(randomTokenPort);
        this.clock = Objects.requireNonNull(clock);
        this.enqueueEmail = Objects.requireNonNull(enqueueEmail);
    }

    /**
     * Verifies a user's email address using an opaque token.
     * <p>
     * Ensures the token exists, is unused, not expired, and corresponds to a
     * valid user; then marks the user as verified and the token as used.
     *
     * @param opaqueToken base64url opaque token from the verification link
     * @throws InvalidVerificationToken if token is missing/invalid
     * @throws TokenAlreadyUsed         if token was already used
     * @throws ExpiredVerificationToken if token expired
     */
    public void execute(String opaqueToken) {
        log.info("verify_email_start");
        if (opaqueToken == null || opaqueToken.isBlank()) {
            throw new InvalidVerificationToken("Token is required");
        }
        String hash = randomTokenPort.sha256(opaqueToken.trim());
        EmailVerificationToken token = tokenRepository.findByHash(hash)
                .orElseThrow(() -> new InvalidVerificationToken("Invalid verification token"));
        log.info("verify_email_token_loaded", "tokenId", token.id().toString(), "userId", token.userId().toString());

        Instant now = clock.now();
        if (token.isUsed()) {
            log.info("verify_email_token_used", "tokenId", token.id().toString());
            throw new TokenAlreadyUsed("Verification token already used");
        }
        if (token.expiresAt().isBefore(now)) {
            log.info("verify_email_token_expired", "tokenId", token.id().toString(), "expiredAt", token.expiresAt().toString());
            throw new ExpiredVerificationToken("Verification token expired");
        }

        User user = userRepository.findById(token.userId())
                .orElseThrow(() -> new InvalidVerificationToken("Invalid token user"));
        log.info("verify_email_user_loaded", "userId", user.getId().toString(), "emailVerified", user.isEmailVerified());

        User verified = user.markEmailVerified(now);
        userRepository.update(verified);
        tokenRepository.markUsed(token.id(), now);
        log.info("verify_email_success", "userId", user.getId().toString(), "tokenId", token.id().toString());

        // Dispatch welcome email
        var welcomeJobBuilder = WelcomeEmailJob.builder()
                .to(new EmailAddress(user.getEmail().value()));
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            welcomeJobBuilder = welcomeJobBuilder.userName(user.getFirstName());
        }
        enqueueEmail.enqueue(welcomeJobBuilder.build());
    }
}
