package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.service.audit.AuditEvents;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.factory.RefreshTokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.JwtAccessTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Logs a user in via a federated identity provider (e.g., Google), creating the user if needed,
 * issuing an access token and creating a refresh-token session.
 */
public class SocialLoginUseCase {
    private final UserRepositoryPort users;
    private final SessionRepositoryPort sessions;
    private final JwtAccessTokenPort jwtAccessTokenPort;
    private final RefreshTokenFactory refreshTokenFactory;
    private final AuditEventPort audit;
    private final ClockPort clock;
    private final Duration refreshTtl;

    public SocialLoginUseCase(UserRepositoryPort users,
                              SessionRepositoryPort sessions,
                              JwtAccessTokenPort jwtAccessTokenPort,
                              RefreshTokenFactory refreshTokenFactory,
                              AuditEventPort audit,
                              ClockPort clock,
                              Duration refreshTtl) {
        this.users = users;
        this.sessions = sessions;
        this.jwtAccessTokenPort = jwtAccessTokenPort;
        this.refreshTokenFactory = refreshTokenFactory;
        this.audit = audit;
        this.clock = clock;
        this.refreshTtl = refreshTtl;
    }

    public Result execute(String emailRaw, String firstName, String lastName, boolean emailVerified, String userAgent, InetAddress ip) {
        Email email = Email.of(emailRaw);
        Instant now = clock.now();
        User user = users.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User(
                    UserId.newId(),
                    email,
                    firstName,
                    lastName,
                    UserStatus.ACTIVE,
                    emailVerified,
                    null,
                    now,
                    now,
                    Set.of(Role.CUSTOMER)
            );
            user = users.save(user);
        } else {
            boolean changed = false;
            if (firstName != null && !firstName.equals(user.getFirstName())) changed = true;
            if (lastName != null && !lastName.equals(user.getLastName())) changed = true;
            if (emailVerified && !user.isEmailVerified()) changed = true;
            if (changed) {
                user = new User(user.getId(), user.getEmail(),
                        firstName == null ? user.getFirstName() : firstName,
                        lastName == null ? user.getLastName() : lastName,
                        user.getStatus(),
                        emailVerified || user.isEmailVerified(),
                        user.getPasswordHash(),
                        user.getCreatedAt(),
                        now,
                        user.getRoles());
                user = users.update(user);
            }
        }

        RefreshTokenFactory.Pair refresh = refreshTokenFactory.generate();
        Instant expiresAt = now.plus(refreshTtl);
        Session session = Session.createNew(user.getId(), refresh.hash(), userAgent, ip, now, expiresAt);
        session = sessions.save(session);

        java.util.List<String> roleNames = user.getRoles().stream().map(Enum::name).toList();
        String accessToken = jwtAccessTokenPort.createAccessToken(user.getId().value().toString(), roleNames, null);
        long expiresIn = jwtAccessTokenPort.getAccessTokenTtlSeconds();
        audit.record(user.getId(), AuditEvents.LOGIN_SUCCESS, java.util.Map.of(
                "sessionId", session.getId() == null ? "" : session.getId().toString(),
                "ip", ip == null ? "" : ip.getHostAddress(),
                "ua", userAgent == null ? "" : userAgent,
                "method", "google_oauth"
        ), now);

        return new Result(accessToken, expiresIn, refresh.token());
    }

    public record Result(String accessToken, long expiresInSeconds, String refreshToken) {}
}

