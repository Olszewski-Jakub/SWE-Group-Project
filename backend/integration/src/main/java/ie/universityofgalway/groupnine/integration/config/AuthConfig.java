package ie.universityofgalway.groupnine.integration.config;

import ie.universityofgalway.groupnine.security.config.props.AuthProps;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.factory.RefreshTokenFactory;
import ie.universityofgalway.groupnine.service.auth.factory.TokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.BruteForceGuardPort;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.JwtAccessTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.VerificationTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.usecase.LoginUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutAllUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RefreshUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RegisterUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.VerifyEmailUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RequestPasswordResetUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.ResetPasswordUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.ChangePasswordUseCase;
import ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordResetTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.security.port.PasswordResetRateLimitPort;
import ie.universityofgalway.groupnine.service.session.usecase.GetSessionChainUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableConfigurationProperties(AuthProps.class)
public class AuthConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            @Autowired UserRepositoryPort userRepository,
            @Autowired VerificationTokenRepositoryPort tokenRepository,
            @Autowired PasswordHasherPort passwordHasher,
            @Autowired ClockPort clock,
            @Autowired EnqueueEmailPort enqueueEmail,
            @Autowired TokenFactory tokenFactory,
            @Autowired @Value("${app.base-url}") String appBaseUrl
    ) {
        return new RegisterUserUseCase(
                userRepository,
                tokenRepository,
                passwordHasher,
                clock,
                enqueueEmail,
                tokenFactory,
                appBaseUrl
        );
    }

    @Bean
    public VerifyEmailUseCase verifyEmailUseCase(
            @Autowired VerificationTokenRepositoryPort tokenRepository,
            @Autowired UserRepositoryPort userRepository,
            @Autowired RandomTokenPort randomTokenPort,
            @Autowired ClockPort clock,
            @Autowired EnqueueEmailPort enqueueEmail
    ) {
        return new VerifyEmailUseCase(tokenRepository, userRepository, randomTokenPort, clock, enqueueEmail);
    }

    @Bean
    public RequestPasswordResetUseCase requestPasswordResetUseCase(
            @Autowired UserRepositoryPort userRepository,
            @Autowired PasswordResetTokenRepositoryPort tokenRepository,
            @Autowired RandomTokenPort randomTokenPort,
            @Autowired ClockPort clock,
            @Autowired EnqueueEmailPort enqueueEmail,
            @Autowired PasswordResetRateLimitPort rateLimit,
            @Autowired @Value("${app.base-url}") String appBaseUrl,
            @Autowired @Value("${passwordReset.ttlMinutes:30}") long ttlMinutes
    ) {
        return new RequestPasswordResetUseCase(
                userRepository,
                tokenRepository,
                randomTokenPort,
                clock,
                enqueueEmail,
                rateLimit,
                appBaseUrl,
                java.time.Duration.ofMinutes(ttlMinutes)
        );
    }

    @Bean
    public ResetPasswordUseCase resetPasswordUseCase(
            @Autowired PasswordResetTokenRepositoryPort tokenRepository,
            @Autowired UserRepositoryPort userRepository,
            @Autowired PasswordHasherPort passwordHasher,
            @Autowired RandomTokenPort randomTokenPort,
            @Autowired SessionRepositoryPort sessionRepository,
            @Autowired ClockPort clock
    ) {
        return new ResetPasswordUseCase(
                tokenRepository,
                userRepository,
                passwordHasher,
                randomTokenPort,
                sessionRepository,
                clock
        );
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(
            @Autowired UserRepositoryPort userRepository,
            @Autowired PasswordHasherPort passwordHasher,
            @Autowired SessionRepositoryPort sessionRepository,
            @Autowired ClockPort clock
    ) {
        return new ChangePasswordUseCase(
                userRepository,
                passwordHasher,
                sessionRepository,
                clock
        );
    }

    @Bean
    public RefreshTokenFactory refreshTokenFactory(@Autowired RandomTokenPort randomTokenPort) {
        return new RefreshTokenFactory(randomTokenPort);
    }

    @Bean
    public LoginUseCase loginUseCase(
            @Autowired UserRepositoryPort userRepository,
            @Autowired PasswordHasherPort passwordHasher,
            @Autowired SessionRepositoryPort sessionRepository,
            @Autowired JwtAccessTokenPort jwtAccessTokenPort,
            @Autowired BruteForceGuardPort bruteForceGuard,
            @Autowired RefreshTokenFactory refreshTokenFactory,
            @Autowired AuditEventPort auditEventPort,
            @Autowired ClockPort clock,
            @Autowired AuthProps authProps
    ) {
        return new LoginUseCase(
                userRepository,
                passwordHasher,
                sessionRepository,
                jwtAccessTokenPort,
                bruteForceGuard,
                refreshTokenFactory,
                auditEventPort,
                clock,
                java.time.Duration.ofDays(authProps.getRefreshTtlDays())
        );
    }

    @Bean
    public RefreshUseCase refreshUseCase(
            @Autowired SessionRepositoryPort sessionRepository,
            @Autowired JwtAccessTokenPort jwtAccessTokenPort,
            @Autowired RefreshTokenFactory refreshTokenFactory,
            @Autowired RandomTokenPort randomTokenPort,
            @Autowired AuditEventPort auditEventPort,
            @Autowired ClockPort clock,
            @Autowired AuthProps authProps,
            @Autowired UserRepositoryPort userRepository
    ) {
        return new RefreshUseCase(
                sessionRepository,
                jwtAccessTokenPort,
                refreshTokenFactory,
                randomTokenPort,
                auditEventPort,
                clock,
                java.time.Duration.ofDays(authProps.getRefreshTtlDays()),
                userRepository
        );
    }

    @Bean
    public LogoutUseCase logoutUseCase(
            @Autowired SessionRepositoryPort sessionRepository,
            @Autowired RandomTokenPort randomTokenPort,
            @Autowired AuditEventPort auditEventPort,
            @Autowired ClockPort clock
    ) {
        return new LogoutUseCase(sessionRepository, randomTokenPort, auditEventPort, clock);
    }

    @Bean
    public LogoutAllUseCase logoutAllUseCase(
            @Autowired SessionRepositoryPort sessionRepository,
            @Autowired RandomTokenPort randomTokenPort,
            @Autowired AuditEventPort auditEventPort,
            @Autowired ClockPort clock
    ) {
        return new LogoutAllUseCase(sessionRepository, randomTokenPort, auditEventPort, clock);
    }

    @Bean
    public GetSessionChainUseCase getSessionChainUseCase(
            @Autowired SessionRepositoryPort sessionRepository,
            @Autowired RandomTokenPort randomTokenPort
    ) {
        return new GetSessionChainUseCase(sessionRepository, randomTokenPort);
    }

    @Bean
    public ie.universityofgalway.groupnine.service.auth.usecase.AssignRoleToUserUseCase assignRoleToUserUseCase(
            @Autowired UserRepositoryPort userRepository,
            @Autowired ClockPort clock
    ) {
        return new ie.universityofgalway.groupnine.service.auth.usecase.AssignRoleToUserUseCase(userRepository, clock);
    }

    @Bean
    public ie.universityofgalway.groupnine.service.auth.usecase.RevokeRoleFromUserUseCase revokeRoleFromUserUseCase(
            @Autowired UserRepositoryPort userRepository,
            @Autowired ClockPort clock
    ) {
        return new ie.universityofgalway.groupnine.service.auth.usecase.RevokeRoleFromUserUseCase(userRepository, clock);
    }

    @Bean
    public ie.universityofgalway.groupnine.service.auth.usecase.GetUserRolesUseCase getUserRolesUseCase(
            @Autowired UserRepositoryPort userRepository
    ) {
        return new ie.universityofgalway.groupnine.service.auth.usecase.GetUserRolesUseCase(userRepository);
    }
}
