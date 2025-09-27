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
import ie.universityofgalway.groupnine.service.audit.AuditEvents;
import ie.universityofgalway.groupnine.service.auth.factory.RefreshTokenFactory;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.port.*;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Authenticates a user, issues an access token and creates a refresh-token session.
 */
public class LoginUseCase {
    public record Result(String accessToken, long expiresInSeconds, String refreshToken) {}

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final SessionRepositoryPort sessionRepository;
    private final JwtAccessTokenPort jwtAccessTokenPort;
    private final BruteForceGuardPort bruteForceGuard;
    private final RefreshTokenFactory refreshTokenFactory;
    private final AuditEventPort audit;
    private final ClockPort clock;
    private final Duration refreshTtl;

    public LoginUseCase(UserRepositoryPort userRepository,
                        PasswordHasherPort passwordHasher,
                        SessionRepositoryPort sessionRepository,
                        JwtAccessTokenPort jwtAccessTokenPort,
                        BruteForceGuardPort bruteForceGuard,
                        RefreshTokenFactory refreshTokenFactory,
                        AuditEventPort audit,
                        ClockPort clock,
                        Duration refreshTtl) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.sessionRepository = sessionRepository;
        this.jwtAccessTokenPort = jwtAccessTokenPort;
        this.bruteForceGuard = bruteForceGuard;
        this.refreshTokenFactory = refreshTokenFactory;
        this.audit = audit;
        this.clock = clock;
        this.refreshTtl = refreshTtl;
    }

    /**
     * Perform a login attempt.
     * @param emailRaw user email address
     * @param password raw password
     * @param userAgent caller user-agent (optional)
     * @param ipAddress caller IP address (optional)
     * @return access token payload including a new refresh token
     */
    public Result execute(String emailRaw, String password, String userAgent, InetAddress ipAddress) {
        Email email = Email.of(emailRaw);
        BruteForceGuardPort.Verdict verdict = bruteForceGuard.check(email, ipAddress);
        if (!verdict.allowed()) {
            audit.record(null, AuditEvents.LOGIN_FAILED, java.util.Map.of(
                    "email", email.value(),
                    "reason", "rate_limited"
            ), clock.now());
            Long retry = verdict.retryAfterSeconds();
            long retryAfter = retry == null ? 60L : Math.max(0L, retry);
            throw new TooManyAttempts("Too many attempts. Retry after " + retryAfter + " seconds.", retryAfter);
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            bruteForceGuard.recordFailure(email, ipAddress);
            audit.record(null, AuditEvents.LOGIN_FAILED, java.util.Map.of(
                    "email", email.value(),
                    "reason", "not_found"
            ), clock.now());
            throw new InvalidCredentials("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserLocked("User account is not active");
        }
        if (!user.isEmailVerified()) {
            throw new UserNotVerified("Email address not verified");
        }
        if (user.getPasswordHash() == null || !passwordHasher.matches(password, user.getPasswordHash())) {
            bruteForceGuard.recordFailure(email, ipAddress);
            audit.record(user.getId(), AuditEvents.LOGIN_FAILED, java.util.Map.of(
                    "email", email.value(),
                    "reason", "bad_password"
            ), clock.now());
            throw new InvalidCredentials("Invalid email or password");
        }

        bruteForceGuard.recordSuccess(email, ipAddress);

        RefreshTokenFactory.Pair refresh = refreshTokenFactory.generate();
        Instant now = clock.now();
        Instant expiresAt = now.plus(refreshTtl);
        Session session = Session.createNew(UserId.of(user.getId().value()), refresh.hash(), userAgent, ipAddress, now, expiresAt);
        session = sessionRepository.save(session);

        String accessToken = jwtAccessTokenPort.createAccessToken(user.getId().value().toString(), List.of(), null);
        long expiresIn = jwtAccessTokenPort.getAccessTokenTtlSeconds();
        audit.record(user.getId(), AuditEvents.LOGIN_SUCCESS, java.util.Map.of(
                "sessionId", session.getId() == null ? "" : session.getId().toString(),
                "ip", ipAddress == null ? "" : ipAddress.getHostAddress(),
                "ua", userAgent == null ? "" : userAgent
        ), now);
        return new Result(accessToken, expiresIn, refresh.token());
    }
}
