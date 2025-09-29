package ie.universityofgalway.groupnine.infrastructure.security.adapter;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.service.audit.AuditEvents;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.auth.port.BruteForceGuardPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Redis-backed {@link BruteForceGuardPort} implementing a sliding window rate limiter
 * with exponential backoff and a circuit-breaker fallback.
 *
 * <p>Algorithm
 * - Sliding window per principal (email) and per IP using Redis ZSETs. Each failed attempt
 * inserts a member with score=epochMillis. Before counting, entries older than
 * {@code window-seconds} are pruned (ZREMRANGEBYSCORE). If either count reaches the
 * configured threshold, the attempt is blocked and an exponential backoff lock is applied.
 * - Exponential backoff uses a strike counter per dimension to compute a lock TTL with
 * {@code backoff-base-seconds * backoff-factor^(strikes-1)} bounded by {@code backoff-max-seconds}.
 * - Subsequent attempts are immediately blocked while a lock key exists.
 *
 * <p>Keys (with {@code key-prefix}):
 * - ZSET events: {@code {prefix}bfz:e:<email>}, {@code {prefix}bfz:i:<ip>}
 * - Strike counters: {@code {prefix}bfs:e:<email>}, {@code {prefix}bfs:i:<ip>}
 * - Lock flags: {@code {prefix}bfl:e:<email>}, {@code {prefix}bfl:i:<ip>}
 *
 * <p>Configuration (app.security.bruteforce.*):
 * - {@code email-max}, {@code ip-max}, {@code window-seconds}
 * - {@code key-prefix} (for multi-tenant separation)
 * - {@code backoff-base-seconds}, {@code backoff-factor}, {@code backoff-max-seconds}
 *
 * <p>Fallback
 * - On Redis errors, the guard returns an allow verdict and emits an audit event with
 * reason {@code bf_redis_down} so that authentication is not impacted by Redis outages.
 */
@Component
public class RedisBruteForceGuardAdapter implements BruteForceGuardPort {

    private final StringRedisTemplate redis;
    private final AuditEventPort audit;
    private final int emailMax;
    private final int ipMax;
    private final Duration window;
    private final String prefix;
    private final long backoffBaseSeconds;
    private final double backoffFactor;
    private final long backoffMaxSeconds;

    @Autowired
    public RedisBruteForceGuardAdapter(StringRedisTemplate redis,
                                       AuditEventPort audit,
                                       @Value("${app.security.bruteforce.email-max:10}") int emailMax,
                                       @Value("${app.security.bruteforce.ip-max:50}") int ipMax,
                                       @Value("${app.security.bruteforce.window-seconds:900}") long windowSeconds,
                                       @Value("${app.security.bruteforce.key-prefix:app:}") String prefix,
                                       @Value("${app.security.bruteforce.backoff-base-seconds:60}") long backoffBaseSeconds,
                                       @Value("${app.security.bruteforce.backoff-factor:2.0}") double backoffFactor,
                                       @Value("${app.security.bruteforce.backoff-max-seconds:3600}") long backoffMaxSeconds) {
        this.redis = redis;
        this.audit = audit;
        this.emailMax = emailMax;
        this.ipMax = ipMax;
        this.window = Duration.ofSeconds(windowSeconds);
        this.prefix = prefix == null ? "" : prefix;
        this.backoffBaseSeconds = backoffBaseSeconds;
        this.backoffFactor = backoffFactor;
        this.backoffMaxSeconds = backoffMaxSeconds;
    }

    @Override
    public boolean allowAttempt(Email email, InetAddress ipAddress) {
        // Fast-path decision for legacy callers; use {@link #check} for TTL info.
        try {
            long nowMs = System.currentTimeMillis();
            if (exists(lockEmailKey(email)) || exists(lockIpKey(ipAddress))) {
                return false;
            }
            boolean overEmail = overThreshold(emailZKey(email), emailMax, nowMs);
            boolean overIp = overThreshold(ipZKey(ipAddress), ipMax, nowMs);
            if (overEmail || overIp) {
                long lockTtl = computeAndSetBackoff(email, ipAddress);
                audit.record(null, AuditEvents.LOGIN_FAILED, Map.of(
                        "email", email == null ? "" : email.value(),
                        "reason", "rate_limited",
                        "backoffSeconds", lockTtl,
                        "ip", ipAddress == null ? "" : ipAddress.getHostAddress()
                ), Instant.now());
                return false;
            }
            return true;
        } catch (DataAccessException ex) {
            // Circuit breaker: allow when Redis is unavailable
            audit.record(null, AuditEvents.LOGIN_FAILED, Map.of(
                    "reason", "bf_redis_down"
            ), Instant.now());
            return true;
        }
    }

    @Override
    public void recordSuccess(Email email, InetAddress ipAddress) {
        // Clear events, strike counters and locks on successful authentication.
        try {
            del(emailZKey(email));
            del(ipZKey(ipAddress));
            del(lockEmailKey(email));
            del(lockIpKey(ipAddress));
            del(strikeEmailKey(email));
            del(strikeIpKey(ipAddress));
        } catch (DataAccessException ignored) {
        }
    }

    @Override
    public void recordFailure(Email email, InetAddress ipAddress) {
        // Record a timestamped failure for sliding-window evaluation.
        try {
            long nowMs = System.currentTimeMillis();
            if (email != null) pushEvent(emailZKey(email), nowMs);
            if (ipAddress != null) pushEvent(ipZKey(ipAddress), nowMs);
        } catch (DataAccessException ignored) {
        }
    }

    @Override
    public Long getRetryAfterSeconds(Email email, InetAddress ipAddress) {
        // Return maximum TTL among dimension locks so the client waits long enough.
        try {
            Long a = ttlSeconds(lockEmailKey(email));
            Long b = ttlSeconds(lockIpKey(ipAddress));
            if (a == null) return b;
            if (b == null) return a;
            return Math.max(a, b);
        } catch (DataAccessException e) {
            return null;
        }
    }

    /**
     * Prune expired events and check if the ZSET size within the active window
     * meets or exceeds the configured threshold.
     */
    private boolean overThreshold(String zkey, int max, long nowMs) {
        if (zkey == null) return false;
        long minScore = nowMs - window.toMillis();
        redis.opsForZSet().removeRangeByScore(zkey, 0, minScore);
        Long count = redis.opsForZSet().zCard(zkey);
        return count != null && count >= max;
    }

    /**
     * Insert a failure event (epochMillis + random suffix) and set an expiry on the ZSET.
     */
    private void pushEvent(String zkey, long nowMs) {
        if (zkey == null) return;
        String member = nowMs + ":" + UUID.randomUUID();
        redis.opsForZSet().add(zkey, member, nowMs);
        redis.expire(zkey, window.plusSeconds(backoffMaxSeconds));
    }

    /**
     * Compute exponential backoff for email and IP dimensions and set lock keys accordingly.
     * Returns the effective lock TTL in seconds (max of dimensions).
     */
    private long computeAndSetBackoff(Email email, InetAddress ip) {
        long emailBackoff = email == null ? 0 : nextBackoffSeconds(strikeEmailKey(email), lockEmailKey(email));
        long ipBackoff = ip == null ? 0 : nextBackoffSeconds(strikeIpKey(ip), lockIpKey(ip));
        return Math.max(emailBackoff, ipBackoff);
    }

    /**
     * Increment strike counter, derive TTL using exponential backoff, bound by max, and set lock.
     */
    private long nextBackoffSeconds(String strikeKey, String lockKey) {
        Long strikes = redis.opsForValue().increment(strikeKey);
        if (strikes != null && strikes == 1L) {
            redis.expire(strikeKey, Duration.ofSeconds(backoffMaxSeconds));
        }
        long exp = strikes == null ? 1 : strikes;
        double backoff = backoffBaseSeconds * Math.pow(backoffFactor, Math.max(0, exp - 1));
        long ttl = Math.min((long) Math.ceil(backoff), backoffMaxSeconds);
        redis.opsForValue().set(lockKey, "1", Duration.ofSeconds(ttl));
        return ttl;
    }

    /**
     * Return true if the given Redis key exists.
     */
    private boolean exists(String key) {
        if (key == null) return false;
        Boolean ex = redis.hasKey(key);
        return ex != null && ex;
    }

    /**
     * Delete the given key if not null.
     */
    private void del(String key) {
        if (key != null) redis.delete(key);
    }

    /**
     * Read remaining TTL for a key in seconds; returns null if not applicable.
     */
    private Long ttlSeconds(String key) {
        if (key == null) return null;
        Long s = redis.getExpire(key, java.util.concurrent.TimeUnit.SECONDS);
        if (s == null) return null;
        return s < 0 ? null : s;
    }

    private String emailZKey(Email email) {
        return email == null ? null : prefix + "bfz:e:" + email.value().toLowerCase();
    }

    private String ipZKey(InetAddress ip) {
        return ip == null ? null : prefix + "bfz:i:" + ip.getHostAddress();
    }

    private String strikeEmailKey(Email email) {
        return email == null ? null : prefix + "bfs:e:" + email.value().toLowerCase();
    }

    private String strikeIpKey(InetAddress ip) {
        return ip == null ? null : prefix + "bfs:i:" + ip.getHostAddress();
    }

    private String lockEmailKey(Email email) {
        return email == null ? null : prefix + "bfl:e:" + email.value().toLowerCase();
    }

    private String lockIpKey(InetAddress ip) {
        return ip == null ? null : prefix + "bfl:i:" + ip.getHostAddress();
    }
}
