package ie.universityofgalway.groupnine.infrastructure.security.adapter;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.service.security.port.PasswordResetRateLimitPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory sliding window rate limiter for password reset requests.
 * Intended as a fallback; prefer Redis in production deployments.
 */
@Component
public class InMemoryPasswordResetRateLimitAdapter implements PasswordResetRateLimitPort {

    private final int emailMax;
    private final int ipMax;
    private final Duration window;

    private final Map<String, Deque<Long>> emailEvents = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> ipEvents = new ConcurrentHashMap<>();

    public InMemoryPasswordResetRateLimitAdapter(
            @Value("${app.passwordReset.rateLimit.windowSeconds:900}") long windowSeconds,
            @Value("${app.passwordReset.rateLimit.perEmail:3}") int emailMax,
            @Value("${app.passwordReset.rateLimit.perIp:10}") int ipMax
    ) {
        this.window = Duration.ofSeconds(windowSeconds);
        this.emailMax = emailMax;
        this.ipMax = ipMax;
    }

    @Override
    public boolean allow(Email email, InetAddress ipAddress) {
        long now = Instant.now().toEpochMilli();
        boolean emailOk = underLimit(queueForEmail(email), now, emailMax);
        boolean ipOk = underLimit(queueForIp(ipAddress), now, ipMax);
        return emailOk && ipOk;
    }

    private Deque<Long> queueForEmail(Email email) {
        if (email == null) return new ArrayDeque<>();
        return emailEvents.computeIfAbsent(email.value().toLowerCase(), k -> new ArrayDeque<>());
    }

    private Deque<Long> queueForIp(InetAddress ip) {
        if (ip == null) return new ArrayDeque<>();
        return ipEvents.computeIfAbsent(ip.getHostAddress(), k -> new ArrayDeque<>());
    }

    private boolean underLimit(Deque<Long> dq, long nowMs, int max) {
        if (dq == null) return true;
        prune(dq, nowMs);
        if (dq.size() >= max) {
            return false;
        }
        dq.addLast(nowMs);
        return true;
    }

    private void prune(Deque<Long> dq, long nowMs) {
        long cutoff = nowMs - window.toMillis();
        while (!dq.isEmpty() && dq.peekFirst() < cutoff) {
            dq.pollFirst();
        }
    }
}

