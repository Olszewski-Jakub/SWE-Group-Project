package ie.universityofgalway.groupnine.infrastructure.email;

import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.infrastructure.email.adapter.RedisIdempotencyAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisIdempotencyAdapterTest {
    @Test
    void claimReturnsTrueOnSetIfAbsent() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        var ops = mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(startsWith("idem:"), anyString(), any())).thenReturn(Boolean.TRUE);
        RedisIdempotencyAdapter adapter = new RedisIdempotencyAdapter(redis);
        assertTrue(adapter.claim(new EmailJobId(UUID.randomUUID())));
    }

    @Test
    void claimReturnsFalseWhenAlreadyExists() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        var ops = mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), anyString(), any())).thenReturn(Boolean.FALSE);
        RedisIdempotencyAdapter adapter = new RedisIdempotencyAdapter(redis);
        assertFalse(adapter.claim(new EmailJobId(UUID.randomUUID())));
    }
}

