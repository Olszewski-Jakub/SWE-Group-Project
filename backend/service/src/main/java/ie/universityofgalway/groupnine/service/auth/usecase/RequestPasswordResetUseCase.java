package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.PasswordResetToken;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.jobs.PasswordResetEmailJob;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordResetTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort;
import ie.universityofgalway.groupnine.service.security.port.PasswordResetRateLimitPort;
import ie.universityofgalway.groupnine.util.logging.AppLogger;

import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Use case to initiate a password reset flow. Always returns without disclosing account existence.
 */
public class RequestPasswordResetUseCase {
    private static final AppLogger log = AppLogger.get(RequestPasswordResetUseCase.class);
    private final UserRepositoryPort users;
    private final PasswordResetTokenRepositoryPort tokens;
    private final RandomTokenPort randomTokens;
    private final ClockPort clock;
    private final EnqueueEmailPort enqueueEmail;
    private final PasswordResetRateLimitPort rateLimit;
    private final String appBaseUrl;
    private final Duration ttl;

    public RequestPasswordResetUseCase(UserRepositoryPort users,
                                       PasswordResetTokenRepositoryPort tokens,
                                       RandomTokenPort randomTokens,
                                       ClockPort clock,
                                       EnqueueEmailPort enqueueEmail,
                                       PasswordResetRateLimitPort rateLimit,
                                       String appBaseUrl,
                                       Duration ttl) {
        this.users = Objects.requireNonNull(users);
        this.tokens = Objects.requireNonNull(tokens);
        this.randomTokens = Objects.requireNonNull(randomTokens);
        this.clock = Objects.requireNonNull(clock);
        this.enqueueEmail = Objects.requireNonNull(enqueueEmail);
        this.rateLimit = Objects.requireNonNull(rateLimit);
        this.appBaseUrl = Objects.requireNonNull(appBaseUrl);
        this.ttl = Objects.requireNonNull(ttl);
    }

    /**
     * Initiate a password reset if the account exists.
     * The method is constant-like time: it applies delays even when user doesn't exist.
     */
    public void execute(String emailRaw, String localeTag, InetAddress ipAddress) {
        long start = System.nanoTime();
        Locale locale = (localeTag == null || localeTag.isBlank()) ? Locale.forLanguageTag("en-IE") : Locale.forLanguageTag(localeTag);
        Email email;
        try {
            email = Email.of(emailRaw);
        } catch (Exception e) {
            // Invalid email: simulate work and return; controller should handle validation when appropriate
            constantTimeDelay(start);
            return;
        }

        if (!rateLimit.allow(email, ipAddress)) {
            log.info("pwd_reset_rate_limited", "email", email.value(), "ip", ipAddress == null ? "" : ipAddress.getHostAddress());
            constantTimeDelay(start);
            return;
        }

        Optional<User> maybeUser = users.findByEmail(email);
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            Instant now = clock.now();
            tokens.invalidateAllForUser(user.getId(), now);
            String opaque = randomTokens.generateOpaqueToken();
            String hash = randomTokens.sha256(opaque);
            Instant expiresAt = now.plus(ttl);
            PasswordResetToken token = PasswordResetToken.createNew(user.getId(), hash, expiresAt, now);
            tokens.save(token);

            String resetUrl = appBaseUrl.replaceAll("/$", "") + "/reset-password?token=" + opaque;
            PasswordResetEmailJob.OptionalStep job = PasswordResetEmailJob.builder()
                    .to(new EmailAddress(user.getEmail().value()))
                    .resetLink(URI.create(resetUrl))
                    .locale(locale);
            if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
                job = job.userName(user.getFirstName());
            }
            enqueueEmail.enqueue(job.build());
            log.info("pwd_reset_enqueued", "userId", user.getId().toString());
        }

        constantTimeDelay(start);
    }

    private void constantTimeDelay(long startNs) {
        long targetMs = 120; // aim for ~120ms total, add small jitter
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
        long remaining = targetMs - elapsedMs;
        if (remaining < 20) remaining = 20;
        remaining += (long) (Math.random() * 30); // jitter up to 30ms
        try { Thread.sleep(Math.max(10, remaining)); } catch (InterruptedException ignored) { }
    }
}

