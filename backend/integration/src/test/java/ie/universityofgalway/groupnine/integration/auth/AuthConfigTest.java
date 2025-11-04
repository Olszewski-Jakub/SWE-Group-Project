package ie.universityofgalway.groupnine.integration.auth;

import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.integration.config.AuthConfig;
import ie.universityofgalway.groupnine.security.config.props.AuthProps;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.factory.RefreshTokenFactory;
import ie.universityofgalway.groupnine.service.auth.factory.TokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.BruteForceGuardPort;
import ie.universityofgalway.groupnine.service.auth.port.GoogleOAuthClientPort;
import ie.universityofgalway.groupnine.service.auth.port.GoogleOAuthConfigPort;
import ie.universityofgalway.groupnine.service.auth.port.OAuthStatePort;
import ie.universityofgalway.groupnine.domain.auth.OAuthProvider;
import ie.universityofgalway.groupnine.service.auth.port.OAuthAccountRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.JwtAccessTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordResetTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.VerificationTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.usecase.ChangePasswordUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LoginUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutAllUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RefreshUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RegisterUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RequestPasswordResetUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.ResetPasswordUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.VerifyEmailUseCase;
import ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort;
import ie.universityofgalway.groupnine.service.security.port.PasswordResetRateLimitPort;
import ie.universityofgalway.groupnine.service.session.usecase.GetSessionChainUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig
@Import({AuthConfig.class, AuthConfigTest.Deps.class})
@TestPropertySource(properties = {
        "app.base-url=http://localhost:8080",
        "passwordReset.ttlMinutes=15"
})
class AuthConfigTest {

    @TestConfiguration
    static class Deps {
        @Bean
        UserRepositoryPort userRepositoryPort() {
            return new UserRepositoryPort() {
                @Override
                public boolean existsByEmail(ie.universityofgalway.groupnine.domain.user.Email email) {
                    return false;
                }

                @Override
                public java.util.Optional<ie.universityofgalway.groupnine.domain.user.User> findByEmail(ie.universityofgalway.groupnine.domain.user.Email email) {
                    return java.util.Optional.empty();
                }

                @Override
                public java.util.Optional<ie.universityofgalway.groupnine.domain.user.User> findById(ie.universityofgalway.groupnine.domain.user.UserId id) {
                    return java.util.Optional.empty();
                }

                @Override
                public ie.universityofgalway.groupnine.domain.user.User save(ie.universityofgalway.groupnine.domain.user.User user) {
                    return user;
                }

                @Override
                public ie.universityofgalway.groupnine.domain.user.User update(ie.universityofgalway.groupnine.domain.user.User user) {
                    return user;
                }
            };
        }

        @Bean
        VerificationTokenRepositoryPort verificationTokenRepositoryPort() {
            return new VerificationTokenRepositoryPort() {
                @Override
                public ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken save(ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken token) {
                    return token;
                }

                @Override
                public java.util.Optional<ie.universityofgalway.groupnine.domain.auth.EmailVerificationToken> findByHash(String tokenHash) {
                    return java.util.Optional.empty();
                }

                @Override
                public void markUsed(java.util.UUID tokenId, java.time.Instant usedAt) {
                }
            };
        }

        @Bean
        PasswordHasherPort passwordHasherPort() {
            return new PasswordHasherPort() {
                @Override
                public String hash(String rawPassword) {
                    return "hash";
                }

                @Override
                public boolean matches(String rawPassword, String hash) {
                    return true;
                }
            };
        }

        @Bean
        ClockPort clockPort() {
            return () -> java.time.Instant.now();
        }

        @Bean
        EnqueueEmailPort enqueueEmailPort() {
            return job -> {
            };
        }

        @Bean
        RandomTokenPort randomTokenPort() {
            return new RandomTokenPort() {
                @Override
                public String generateOpaqueToken() {
                    return "tok";
                }

                @Override
                public String sha256(String token) {
                    return "hash";
                }
            };
        }

        @Bean
        TokenFactory tokenFactory(RandomTokenPort randomTokenPort) {
            return new TokenFactory(randomTokenPort);
        }

        @Bean
        SessionRepositoryPort sessionRepositoryPort() {
            return new SessionRepositoryPort() {
                @Override
                public Session save(Session session) {
                    return session;
                }

                @Override
                public java.util.Optional<Session> findByRefreshTokenHash(String hash) {
                    return java.util.Optional.empty();
                }

                @Override
                public java.util.Optional<Session> findById(java.util.UUID id) {
                    return java.util.Optional.empty();
                }

                @Override
                public void revokeSession(java.util.UUID sessionId, java.time.Instant revokedAt, String reason, java.util.UUID replacedBy) {
                }

                @Override
                public void revokeAllForUser(ie.universityofgalway.groupnine.domain.user.UserId userId, java.time.Instant revokedAt, String reason) {
                }

                @Override
                public void revokeChainFrom(java.util.UUID startSessionId, java.time.Instant revokedAt, String reason) {
                }
            };
        }

        @Bean
        JwtAccessTokenPort jwtAccessTokenPort() {
            return new JwtAccessTokenPort() {
                @Override
                public String createAccessToken(String subjectUserId, java.util.List<String> roles, java.util.Map<String, Object> extraClaims) {
                    return "jwt";
                }

                @Override
                public long getAccessTokenTtlSeconds() {
                    return 3600L;
                }
            };
        }

        @Bean
        BruteForceGuardPort bruteForceGuardPort() {
            return new BruteForceGuardPort() {
                @Override
                public boolean allowAttempt(ie.universityofgalway.groupnine.domain.user.Email email, java.net.InetAddress ipAddress) {
                    return true;
                }

                @Override
                public void recordSuccess(ie.universityofgalway.groupnine.domain.user.Email email, java.net.InetAddress ipAddress) {
                }

                @Override
                public void recordFailure(ie.universityofgalway.groupnine.domain.user.Email email, java.net.InetAddress ipAddress) {
                }
            };
        }

        @Bean
        AuditEventPort auditEventPort() {
            return (userId, type, metadata, createdAt) -> {
            };
        }

        @Bean
        PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort() {
            return new PasswordResetTokenRepositoryPort() {
                @Override
                public ie.universityofgalway.groupnine.domain.auth.PasswordResetToken save(ie.universityofgalway.groupnine.domain.auth.PasswordResetToken token) {
                    return token;
                }

                @Override
                public void invalidateAllForUser(ie.universityofgalway.groupnine.domain.user.UserId userId, java.time.Instant when) {
                }

                @Override
                public java.util.Optional<ie.universityofgalway.groupnine.domain.auth.PasswordResetToken> findByHash(String tokenHash) {
                    return java.util.Optional.empty();
                }

                @Override
                public void markUsed(java.util.UUID tokenId, java.time.Instant usedAt) {
                }
            };
        }

        @Bean
        PasswordResetRateLimitPort passwordResetRateLimitPort() {
            return (email, ip) -> true;
        }

        @Bean
        AuthProps authProps() {
            var p = new AuthProps();
            p.setRefreshTtlDays(14);
            return p;
        }

        // Stubs for Google OAuth-related ports required by AuthConfig
        @Bean
        GoogleOAuthConfigPort googleOAuthConfigPort() {
            return new GoogleOAuthConfigPort() {
                @Override
                public String getAuthorizationUri() { return "https://accounts.google.com/o/oauth2/v2/auth"; }
                @Override
                public String getTokenUri() { return "https://oauth2.googleapis.com/token"; }
                @Override
                public String getUserInfoUri() { return "https://openidconnect.googleapis.com/v1/userinfo"; }
                @Override
                public String getClientId() { return "test-client-id"; }
                @Override
                public String getClientSecret() { return "test-client-secret"; }
                @Override
                public String getRedirectUri() { return "http://localhost/callback"; }
                @Override
                public java.util.List<String> getScopes() { return java.util.List.of("openid", "email", "profile"); }
                @Override
                public java.util.List<String> getAllowedRedirects() { return java.util.List.of("http://localhost"); }
            };
        }

        @Bean
        OAuthStatePort oAuthStatePort() {
            return new OAuthStatePort() {
                @Override
                public String create(String redirect) { return "state-token"; }
                @Override
                public Parsed verify(String stateToken) { return new Parsed("nonce", null); }
            };
        }

        @Bean
        GoogleOAuthClientPort googleOAuthClientPort() {
            return new GoogleOAuthClientPort() {
                @Override
                public TokenResponse exchangeCode(String code) { return new TokenResponse(); }
                @Override
                public UserInfo getUserInfo(String accessToken) { return new UserInfo(); }
            };
        }

        @Bean
        OAuthAccountRepositoryPort oauthAccountRepositoryPort() {
            return new OAuthAccountRepositoryPort() {
                @Override
                public boolean isLinked(OAuthProvider provider, java.util.UUID userId) { return false; }
                @Override
                public boolean existsByProviderUserId(OAuthProvider provider, String providerUserId) { return false; }
                @Override
                public void link(OAuthProvider provider, String providerUserId, String email, java.util.UUID userId) { }
            };
        }
    }

    @Autowired
    RegisterUserUseCase register;
    @Autowired
    VerifyEmailUseCase verify;
    @Autowired
    RequestPasswordResetUseCase reqReset;
    @Autowired
    ResetPasswordUseCase reset;
    @Autowired
    ChangePasswordUseCase change;
    @Autowired
    RefreshTokenFactory refreshFactory;
    @Autowired
    LoginUseCase login;
    @Autowired
    RefreshUseCase refresh;
    @Autowired
    LogoutUseCase logout;
    @Autowired
    LogoutAllUseCase logoutAll;
    @Autowired
    GetSessionChainUseCase getChain;

    @Test
    void beansCreated() {
        assertNotNull(register);
        assertNotNull(verify);
        assertNotNull(reqReset);
        assertNotNull(reset);
        assertNotNull(change);
        assertNotNull(refreshFactory);
        assertNotNull(login);
        assertNotNull(refresh);
        assertNotNull(logout);
        assertNotNull(logoutAll);
        assertNotNull(getChain);
    }
}
