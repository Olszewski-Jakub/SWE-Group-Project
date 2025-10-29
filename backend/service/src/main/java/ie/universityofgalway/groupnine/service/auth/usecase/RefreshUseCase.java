package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.exception.ExpiredRefreshToken;
import ie.universityofgalway.groupnine.domain.auth.exception.InvalidRefreshToken;
import ie.universityofgalway.groupnine.domain.auth.exception.RefreshReuseDetected;
import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.factory.RefreshTokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.JwtAccessTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Exchanges a valid refresh token for a new access token and a rotated refresh token.
 * Enforces rotation and detects token reuse.
 */
public class RefreshUseCase {
    private final SessionRepositoryPort sessionRepository;
    private final JwtAccessTokenPort jwtAccessTokenPort;
    private final RefreshTokenFactory refreshTokenFactory;
    private final RandomTokenPort randomTokenPort;
    private final AuditEventPort audit;
    private final ClockPort clock;
    private final Duration refreshTtl;
    private final ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort userRepository;
    public RefreshUseCase(SessionRepositoryPort sessionRepository,
                          JwtAccessTokenPort jwtAccessTokenPort,
                          RefreshTokenFactory refreshTokenFactory,
                          RandomTokenPort randomTokenPort,
                          AuditEventPort audit,
                          ClockPort clock,
                          Duration refreshTtl,
                          ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort userRepository) {
        this.sessionRepository = sessionRepository;
        this.jwtAccessTokenPort = jwtAccessTokenPort;
        this.refreshTokenFactory = refreshTokenFactory;
        this.randomTokenPort = randomTokenPort;
        this.audit = audit;
        this.clock = clock;
        this.refreshTtl = refreshTtl;
        this.userRepository = userRepository;
    }

    /**
     * Backward-compatible constructor retained for test stability.
     * <p>
     * Delegates to the main constructor with a minimal inline {@code UserRepositoryPort}
     * that returns a dummy active user with no roles. Production code should use the full
     * constructor that supplies a {@code UserRepositoryPort}.
     */
    public RefreshUseCase(SessionRepositoryPort sessionRepository,
                          JwtAccessTokenPort jwtAccessTokenPort,
                          RefreshTokenFactory refreshTokenFactory,
                          RandomTokenPort randomTokenPort,
                          AuditEventPort audit,
                          ClockPort clock,
                          Duration refreshTtl) {
        this(
                sessionRepository,
                jwtAccessTokenPort,
                refreshTokenFactory,
                randomTokenPort,
                audit,
                clock,
                refreshTtl,
                new ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort() {
                    @Override
                    public boolean existsByEmail(ie.universityofgalway.groupnine.domain.user.Email email) { return false; }

                    @Override
                    public java.util.Optional<ie.universityofgalway.groupnine.domain.user.User> findByEmail(ie.universityofgalway.groupnine.domain.user.Email email) { return java.util.Optional.empty(); }

                    @Override
                    public java.util.Optional<ie.universityofgalway.groupnine.domain.user.User> findById(ie.universityofgalway.groupnine.domain.user.UserId id) {
                        return java.util.Optional.of(new ie.universityofgalway.groupnine.domain.user.User(
                                ie.universityofgalway.groupnine.domain.user.UserId.of(id.value()),
                                ie.universityofgalway.groupnine.domain.user.Email.of("dummy@example.com"),
                                "",
                                "",
                                ie.universityofgalway.groupnine.domain.user.UserStatus.ACTIVE,
                                true,
                                null,
                                java.time.Instant.EPOCH,
                                java.time.Instant.EPOCH,
                                java.util.Set.of()
                        ));
                    }

                    @Override
                    public ie.universityofgalway.groupnine.domain.user.User save(ie.universityofgalway.groupnine.domain.user.User user) { return user; }

                    @Override
                    public ie.universityofgalway.groupnine.domain.user.User update(ie.universityofgalway.groupnine.domain.user.User user) { return user; }
                }
        );
    }

    /**
     * Rotate a refresh token and issue a new access token.
     *
     * @param refreshToken opaque refresh token provided by the client
     * @param userAgent    caller user-agent (optional)
     * @param ipAddress    caller IP address (optional)
     * @return new access token and rotated refresh token
     */
    public Result execute(String refreshToken, String userAgent, InetAddress ipAddress) {
        Objects.requireNonNull(refreshToken, "refreshToken");
        String hash = randomTokenPort.sha256(refreshToken);
        Session existing = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshToken("Unknown refresh token"));

        Instant now = clock.now();
        if (existing.getExpiresAt().isBefore(now) || existing.getExpiresAt().equals(now)) {
            throw new ExpiredRefreshToken("Refresh token expired");
        }
        if (existing.getRevokedAt() != null) {
            // Reuse detected: revoke the forward chain starting from this session for a clear audit trail
            sessionRepository.revokeChainFrom(existing.getId(), now, "refresh_token_reuse_detected_chain_revoked");
            audit.record(existing.getUserId(), ie.universityofgalway.groupnine.service.audit.AuditEvents.REFRESH_REUSE_DETECTED, java.util.Map.of(
                    "sessionId", existing.getId().toString()
            ), now);
            throw new RefreshReuseDetected("Refresh token reuse detected; please re-login");
        }

        // Rotate: revoke the old and create a new session that points back to original
        RefreshTokenFactory.Pair pair = refreshTokenFactory.generate();
        Instant expiresAt = now.plus(refreshTtl);
        // 1) Create the new session (no replaced_by yet)
        Session newSession = Session.createNew(UserId.of(existing.getUserId().value()), pair.hash(), userAgent, ipAddress, now, expiresAt);
        newSession = sessionRepository.save(newSession);
        // 2) Revoke the old session and link it forward to the new one
        sessionRepository.revokeSession(existing.getId(), now, "rotated", newSession.getId());

        var user = userRepository.findById(ie.universityofgalway.groupnine.domain.user.UserId.of(existing.getUserId().value()))
                .orElseThrow(() -> new InvalidRefreshToken("User not found for session"));
        java.util.List<String> roleNames = user.getRoles().stream().map(r -> r.name()).toList();
        String accessToken = jwtAccessTokenPort.createAccessToken(existing.getUserId().value().toString(), roleNames, null);
        long expiresIn = jwtAccessTokenPort.getAccessTokenTtlSeconds();
        audit.record(existing.getUserId(), ie.universityofgalway.groupnine.service.audit.AuditEvents.REFRESH_ROTATED, java.util.Map.of(
                "oldSessionId", existing.getId().toString(),
                "newSessionId", newSession.getId().toString()
        ), now);
        return new Result(accessToken, expiresIn, pair.token());
    }

    public record Result(String accessToken, long expiresInSeconds, String refreshToken) {
    }
}
