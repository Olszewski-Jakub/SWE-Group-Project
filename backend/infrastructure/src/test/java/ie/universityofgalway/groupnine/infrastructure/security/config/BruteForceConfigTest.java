package ie.universityofgalway.groupnine.infrastructure.security.config;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.service.auth.port.BruteForceGuardPort;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

class BruteForceConfigTest {
    @Test
    void providesFallbackBean() {
        BruteForceConfig cfg = new BruteForceConfig();
        BruteForceGuardPort guard = cfg.bruteForceGuardFallback();
        assertNotNull(guard);
        // NoOp guard should allow all
        assertTrue(guard.allowAttempt(Email.of("user@example.com"), InetAddress.getLoopbackAddress()));
    }
}

