package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.auth.exception.InvalidRefreshToken;
import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;

import java.time.Instant;

/**
 * Revokes all currently active refresh-token sessions for the user that owns the provided token.
 */
public class LogoutAllUseCase {
    private final SessionRepositoryPort sessionRepository;
    private final RandomTokenPort randomTokenPort;
    private final ClockPort clock;
    private final AuditEventPort audit;

    public LogoutAllUseCase(SessionRepositoryPort sessionRepository, RandomTokenPort randomTokenPort, AuditEventPort audit, ClockPort clock) {
        this.sessionRepository = sessionRepository;
        this.randomTokenPort = randomTokenPort;
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * Revoke all active sessions for the user owning the provided refresh token.
     */
    public void execute(String currentRefreshToken) {
        String hash = randomTokenPort.sha256(currentRefreshToken);
        Session session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshToken("Unknown refresh token"));
        Instant now = clock.now();
        sessionRepository.revokeAllForUser(session.getUserId(), now, "logout_all");
        audit.record(session.getUserId(), ie.universityofgalway.groupnine.service.audit.AuditEvents.LOGOUT_ALL, java.util.Map.of(), now);
    }
}
