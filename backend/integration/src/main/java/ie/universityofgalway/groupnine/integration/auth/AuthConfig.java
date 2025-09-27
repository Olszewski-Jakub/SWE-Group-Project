package ie.universityofgalway.groupnine.integration.auth;

import ie.universityofgalway.groupnine.security.config.props.AuthProps;
import ie.universityofgalway.groupnine.service.auth.factory.RefreshTokenFactory;
import ie.universityofgalway.groupnine.service.auth.factory.TokenFactory;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.port.*;
import ie.universityofgalway.groupnine.service.auth.usecase.*;
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
            @Autowired EmailSenderPort emailSender,
            @Autowired TokenFactory tokenFactory,
            @Autowired @Value("${app.base-url}") String appBaseUrl
    ) {
        return new RegisterUserUseCase(
                userRepository,
                tokenRepository,
                passwordHasher,
                clock,
                emailSender,
                tokenFactory,
                appBaseUrl
        );
    }

    @Bean
    public VerifyEmailUseCase verifyEmailUseCase(
            @Autowired VerificationTokenRepositoryPort tokenRepository,
            @Autowired UserRepositoryPort userRepository,
            @Autowired RandomTokenPort randomTokenPort,
            @Autowired ClockPort clock
    ) {
        return new VerifyEmailUseCase(tokenRepository, userRepository, randomTokenPort, clock);
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
            @Autowired AuthProps authProps
    ) {
        return new RefreshUseCase(
                sessionRepository,
                jwtAccessTokenPort,
                refreshTokenFactory,
                randomTokenPort,
                auditEventPort,
                clock,
                java.time.Duration.ofDays(authProps.getRefreshTtlDays())
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
}
