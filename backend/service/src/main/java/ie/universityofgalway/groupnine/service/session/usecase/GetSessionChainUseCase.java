package ie.universityofgalway.groupnine.service.session.usecase;

import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Retrieves refresh-token session chains for diagnostics and support tools.
 */
public class GetSessionChainUseCase {
    /**
     * Flat representation of a session in the rotation chain.
     */
    public record SessionNode(UUID id, UUID userId, Instant createdAt, Instant expiresAt, Instant revokedAt, String reason, UUID replacedBySessionId) {}

    private final SessionRepositoryPort sessions;
    private final RandomTokenPort randomTokenPort;

    public GetSessionChainUseCase(SessionRepositoryPort sessions, RandomTokenPort randomTokenPort) {
        this.sessions = sessions;
        this.randomTokenPort = randomTokenPort;
    }

    /**
     * Walk the forward rotation chain starting from a specific session id.
     */
    public List<SessionNode> bySessionId(UUID startId) {
        List<SessionNode> nodes = new ArrayList<>();
        Optional<Session> curOpt = sessions.findById(startId);
        while (curOpt.isPresent()) {
            Session cur = curOpt.get();
            nodes.add(toNode(cur));
            if (cur.getReplacedBySessionId() == null) break;
            curOpt = sessions.findById(cur.getReplacedBySessionId());
        }
        return nodes;
    }

    /**
     * Resolve a refresh token to its owning session and return the chain from there.
     */
    public List<SessionNode> byRefreshToken(String refreshToken) {
        String hash = randomTokenPort.sha256(refreshToken);
        Session start = sessions.findByRefreshTokenHash(hash).orElseThrow();
        return bySessionId(start.getId());
    }

    private SessionNode toNode(Session s) {
        return new SessionNode(
                s.getId(),
                s.getUserId().value(),
                s.getCreatedAt(),
                s.getExpiresAt(),
                s.getRevokedAt(),
                s.getReason(),
                s.getReplacedBySessionId()
        );
    }
}
