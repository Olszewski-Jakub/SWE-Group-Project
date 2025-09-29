package ie.universityofgalway.groupnine.infrastructure.security.adapter;

import ie.universityofgalway.groupnine.domain.user.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NoOpBruteForceGuardAdapterTest {
    @Test
    void alwaysAllowsAndNoops() {
        NoOpBruteForceGuardAdapter guard = new NoOpBruteForceGuardAdapter();
        assertTrue(guard.allowAttempt(Email.of("a@b.com"), null));
        guard.recordFailure(Email.of("a@b.com"), null);
        guard.recordSuccess(Email.of("a@b.com"), null);
    }
}

