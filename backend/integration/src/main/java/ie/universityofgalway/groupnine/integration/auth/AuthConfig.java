package ie.universityofgalway.groupnine.integration.auth;

import ie.universityofgalway.groupnine.service.auth.factory.TokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.EmailSenderPort;
import ie.universityofgalway.groupnine.service.auth.port.PasswordHasherPort;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.VerificationTokenRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.usecase.RegisterUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.VerifyEmailUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
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
}

