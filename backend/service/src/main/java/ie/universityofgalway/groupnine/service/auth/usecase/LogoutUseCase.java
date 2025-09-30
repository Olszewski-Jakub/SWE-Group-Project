package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.InvalidRefreshToken;
import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;

import java.time.Instant;

/**
 * Revokes a single refresh-token session based on a presented refresh token.
 */
public class LogoutUseCase {
    private final SessionRepositoryPort sessionRepository;
    private final RandomTokenPort randomTokenPort;
    private final ClockPort clock;
    private final AuditEventPort audit;

    public LogoutUseCase(SessionRepositoryPort sessionRepository, RandomTokenPort randomTokenPort, AuditEventPort audit, ClockPort clock) {
        this.sessionRepository = sessionRepository;
        this.randomTokenPort = randomTokenPort;
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * Revoke the session that owns the provided refresh token.
     */
    public void execute(String refreshToken) {
        String hash = randomTokenPort.sha256(refreshToken);
        Session session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshToken("Unknown refresh token"));
        Instant now = clock.now();
        sessionRepository.revokeSession(session.getId(), now, "logout", null);
        audit.record(session.getUserId(), ie.universityofgalway.groupnine.service.audit.AuditEvents.LOGOUT, java.util.Map.of(
                "sessionId", session.getId().toString()
        ), now);
    }
}
