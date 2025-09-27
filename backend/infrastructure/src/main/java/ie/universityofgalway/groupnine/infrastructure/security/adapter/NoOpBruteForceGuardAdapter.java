package ie.universityofgalway.groupnine.infrastructure.security.adapter;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.service.auth.port.BruteForceGuardPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * Fallback pass-through implementation used when no concrete {@link BruteForceGuardPort} bean is present.
 */
@Component
@ConditionalOnMissingBean(BruteForceGuardPort.class)
public class NoOpBruteForceGuardAdapter implements BruteForceGuardPort {
    @Override
    public boolean allowAttempt(Email email, InetAddress ipAddress) {
        return true;
    }

    @Override
    public void recordSuccess(Email email, InetAddress ipAddress) {
        // no-op
    }

    @Override
    public void recordFailure(Email email, InetAddress ipAddress) {
        // no-op
    }
}
