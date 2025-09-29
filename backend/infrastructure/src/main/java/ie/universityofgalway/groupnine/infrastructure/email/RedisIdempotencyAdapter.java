package ie.universityofgalway.groupnine.infrastructure.email;

import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis-based implementation of {@link ie.universityofgalway.groupnine.service.email.port.IdempotencyPort}
 * using a simple SETNX with TTL to claim a job id and prevent duplicate processing.
 */
@Component
public class RedisIdempotencyAdapter implements IdempotencyPort {
    private final StringRedisTemplate redis;

    @Autowired
    public RedisIdempotencyAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean claim(EmailJobId id) {
        String key = "idem:" + id.value();
        // 6h TTL by default
        Boolean ok = redis.opsForValue().setIfAbsent(key, "1", Duration.ofHours(6));
        return Boolean.TRUE.equals(ok);
    }
}
