package ie.universityofgalway.groupnine.infrastructure.security.adapter;

import ie.universityofgalway.groupnine.domain.user.Email;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryPasswordResetRateLimitAdapterTest {

    @Test
    void enforcesSlidingWindowPerEmailAndIp() throws Exception {
        // window 1s, max 2 per email, 3 per ip
        InMemoryPasswordResetRateLimitAdapter rl = new InMemoryPasswordResetRateLimitAdapter(1, 2, 3);
        Email email = Email.of("user@example.com");
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        assertTrue(rl.allow(email, ip));
        assertTrue(rl.allow(email, ip));
        assertFalse(rl.allow(email, ip)); // email cap reached
        // Different email, same IP still allowed until ip cap
        assertFalse(rl.allow(Email.of("other@example.com"), ip));
        assertFalse(rl.allow(Email.of("third@example.com"), ip)); // ip cap now reached
    }
}

