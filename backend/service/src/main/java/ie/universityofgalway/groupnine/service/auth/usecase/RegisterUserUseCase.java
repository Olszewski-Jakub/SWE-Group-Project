package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.exception.EmailAlreadyUsed;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.Priority;
import ie.universityofgalway.groupnine.domain.email.jobs.AccountVerificationEmailJob;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.service.auth.factory.TokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.VerificationTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort;
import ie.universityofgalway.groupnine.util.logging.AppLogger;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Registers a new user and sends an email verification link.
 */

/**
 * Use case for registering a new user and initiating email verification.
 */
public class RegisterUserUseCase {
    private static final AppLogger log = AppLogger.get(RegisterUserUseCase.class);
    private final UserRepositoryPort userRepository;
    private final VerificationTokenRepositoryPort tokenRepository;
    private final PasswordHasherPort passwordHasher;
    private final ClockPort clock;
    private final EnqueueEmailPort enqueueEmail;
    private final TokenFactory tokenFactory;
    private final String appBaseUrl;

    public RegisterUserUseCase(UserRepositoryPort userRepository,
                               VerificationTokenRepositoryPort tokenRepository,
                               PasswordHasherPort passwordHasher,
                               ClockPort clock,
                               EnqueueEmailPort enqueueEmail,
                               TokenFactory tokenFactory,
                               String appBaseUrl) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.tokenRepository = Objects.requireNonNull(tokenRepository);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.clock = Objects.requireNonNull(clock);
        this.enqueueEmail = Objects.requireNonNull(enqueueEmail);
        this.tokenFactory = Objects.requireNonNull(tokenFactory);
        this.appBaseUrl = Objects.requireNonNull(appBaseUrl);
    }

    /**
     * Registers a new user and dispatches a verification email.
     * <p>
     * Validates input, enforces unique email, persists the user and a corresponding
     * email verification token, and triggers email delivery with a verification URL.
     *
     * @param emailRaw  raw email address
     * @param password  raw password (min length enforced)
     * @param firstName first name
     * @param lastName  last name
     * @return identifier of the newly created user
     * @throws IllegalArgumentException                                     if inputs are invalid
     * @throws EmailAlreadyUsed if email is taken
     */
    public Result execute(String emailRaw, String password, String firstName, String lastName) {
        log.info("register_user_start", "email", emailRaw);
        if (password == null || password.length() < 10) {
            throw new IllegalArgumentException("Password must be at least 10 characters long");
        }
        Email email = Email.of(emailRaw);

        if (userRepository.existsByEmail(email)) {
            log.info("register_user_email_exists", "email", email.value());
            throw new EmailAlreadyUsed("Email already in use");
        }

        Instant now = clock.now();
        String passwordHash = passwordHasher.hash(password);
        log.debug("register_user_password_hashed");

        User toCreate = User.createNew(email, firstName, lastName, passwordHash, now);
        User saved = userRepository.save(toCreate);
        log.info("register_user_saved", "userId", saved.getId().toString(), "emailVerified", saved.isEmailVerified());

        Instant expires = now.plus(Duration.ofHours(24));
        TokenFactory.VerificationTokenWithOpaque v = tokenFactory.createEmailVerification(saved.getId(), now, expires);
        tokenRepository.save(v.token());
        log.info("register_user_token_created", "userId", saved.getId().toString(), "tokenId", v.token().id().toString(), "expiresAt", v.token().expiresAt().toString());

        String verifyUrl = appBaseUrl.replaceAll("/$", "") + "/verify?token=" + v.opaque();
        var job = AccountVerificationEmailJob.builder()
                .to(new EmailAddress(saved.getEmail().value()))
                .verificationLink(java.net.URI.create(verifyUrl))
                .priority(Priority.HIGH)
                .build();
        enqueueEmail.enqueue(job);
        log.info("register_user_email_dispatched", "userId", saved.getId().toString());

        return new Result(saved.getId().toString());
    }

    public record Result(String userId) {
    }
}
