package ie.universityofgalway.groupnine.infrastructure.security.config;

import ie.universityofgalway.groupnine.infrastructure.security.adapter.NoOpBruteForceGuardAdapter;
import ie.universityofgalway.groupnine.service.auth.port.BruteForceGuardPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BruteForceConfig {

    /**
     * Fallback BruteForceGuard when no concrete implementation is present.
     */
    @Bean
    @ConditionalOnMissingBean(BruteForceGuardPort.class)
    public BruteForceGuardPort bruteForceGuardFallback() {
        return new NoOpBruteForceGuardAdapter();
    }
}

