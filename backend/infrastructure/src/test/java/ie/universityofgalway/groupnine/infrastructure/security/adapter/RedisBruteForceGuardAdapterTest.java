package ie.universityofgalway.groupnine.infrastructure.security.adapter;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.net.InetAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisBruteForceGuardAdapterTest {
    @Test
    void lockKeyBlocksAndTtlIsReturned() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        AuditEventPort audit = mock(AuditEventPort.class);
        ZSetOperations<String, String> zOps = mock(ZSetOperations.class);
        when(redis.opsForZSet()).thenReturn(zOps);
        when(redis.hasKey(startsWith("tst:bfl:e:"))).thenReturn(true);
        when(redis.getExpire(startsWith("tst:bfl:e:"), eq(TimeUnit.SECONDS))).thenReturn(30L);

        RedisBruteForceGuardAdapter guard = new RedisBruteForceGuardAdapter(
                redis, audit,
                1, 50, 60,
                "tst:", 60, 2.0, 120
        );
        var verdict = guard.check(Email.of("a@b.com"), null);
        assertFalse(verdict.allowed());
        assertEquals(30L, verdict.retryAfterSeconds());
    }

    @Test
    void recordSuccessDeletesKeys() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        AuditEventPort audit = mock(AuditEventPort.class);
        RedisBruteForceGuardAdapter guard = new RedisBruteForceGuardAdapter(
                redis, audit, 1, 50, 60, "tst:", 60, 2.0, 120);
        guard.recordSuccess(Email.of("a@b.com"), null);
        verify(redis, atLeastOnce()).delete(startsWith("tst:"));
    }
}

