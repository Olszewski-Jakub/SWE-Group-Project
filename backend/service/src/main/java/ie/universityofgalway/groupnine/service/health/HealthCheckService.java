package ie.universityofgalway.groupnine.service.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Application service implementing {@link HealthCheckUseCase}.
 * <p>
 * Computes overall health from database, Redis, and RabbitMQ probes and exposes
 * an extended report with optional diagnostic details and latency measurements.
 */
@Service
public class HealthCheckService implements HealthCheckUseCase {
    private final DatabaseProbe databaseProbe;
    private final RedisProbe redisProbe;
    private final RabbitProbe rabbitProbe;

    /** Constructs a service with all probes supplied. */
    @Autowired
    public HealthCheckService(DatabaseProbe databaseProbe,
                              RedisProbe redisProbe,
                              RabbitProbe rabbitProbe) {
        this.databaseProbe = databaseProbe;
        this.redisProbe = redisProbe;
        this.rabbitProbe = rabbitProbe;
    }

    /**
     * Backward-compatible constructor for legacy tests that only provide a {@link DatabaseProbe}.
     * Non-database probes are stubbed as healthy so that tests depending solely on DB status
     * remain valid while newer runtime paths use the full-probe constructor.
     */
    public HealthCheckService(DatabaseProbe databaseProbe) {
        this.databaseProbe = databaseProbe;
        this.redisProbe = new RedisProbe() {
            @Override public boolean ping() { return true; }
            @Override public Long dbSize() { return null; }
            @Override public java.util.Map<String, Object> serverInfo() { return java.util.Map.of(); }
        };
        this.rabbitProbe = new RabbitProbe() {
            @Override public boolean ping() { return true; }
            @Override public java.util.Map<String, Object> serverInfo() { return java.util.Map.of(); }
        };
    }

    @Override
    public HealthStatus checkHealth() {
        long t0 = System.nanoTime();
        boolean dbUp = databaseProbe.pingDatabase();
        long dbLatencyMs = (System.nanoTime() - t0) / 1_000_000;

        long t1 = System.nanoTime();
        Boolean rUp = safe(redisProbe::ping);
        boolean redisUp = rUp != null && rUp;
        long redisLatencyMs = (System.nanoTime() - t1) / 1_000_000;

        long t2 = System.nanoTime();
        Boolean rbUp = safe(rabbitProbe::ping);
        boolean rabbitUp = rbUp != null && rbUp;
        long rabbitLatencyMs = (System.nanoTime() - t2) / 1_000_000;
        // Overall UP only if all core deps are UP
        return (dbUp && redisUp && rabbitUp) ? HealthStatus.UP : HealthStatus.DOWN;
    }

    @Override
    public HealthReport checkHealthReport() {
        long t0 = System.nanoTime();
        boolean dbUp = databaseProbe.pingDatabase();
        long dbLatencyMs = (System.nanoTime() - t0) / 1_000_000;

        long t1 = System.nanoTime();
        Boolean rUp = safe(redisProbe::ping);
        boolean redisUp = rUp != null && rUp;
        long redisLatencyMs = (System.nanoTime() - t1) / 1_000_000;

        long t2 = System.nanoTime();
        Boolean rbUp = safe(rabbitProbe::ping);
        boolean rabbitUp = rbUp != null && rbUp;
        long rabbitLatencyMs = (System.nanoTime() - t2) / 1_000_000;

        java.util.Map<String, Object> dbDetails = new java.util.HashMap<>(databaseProbe.details());
        dbDetails.put("latencyMs", dbLatencyMs);
        java.util.Map<String, Object> redisDetails = new java.util.HashMap<>();
        java.util.Map<String, Object> rabbitDetails = new java.util.HashMap<>();

        // Redis stats
        if (redisUp) {
            Long size = safe(redisProbe::dbSize);
            if (size != null) redisDetails.put("dbSize", size);
            java.util.Map<String, Object> info = safe(redisProbe::serverInfo);
            if (info != null) redisDetails.putAll(info);
        }
        redisDetails.put("latencyMs", redisLatencyMs);

        // RabbitMQ stats
        if (rabbitUp) {
            java.util.Map<String, Object> info = safe(rabbitProbe::serverInfo);
            if (info != null) rabbitDetails.putAll(info);
        }
        rabbitDetails.put("latencyMs", rabbitLatencyMs);

        return HealthReport.of(
                (dbUp && redisUp && rabbitUp) ? HealthStatus.UP : HealthStatus.DOWN,
                HealthReport.component(dbUp ? HealthStatus.UP : HealthStatus.DOWN, dbDetails),
                HealthReport.component(redisUp ? HealthStatus.UP : HealthStatus.DOWN, redisDetails),
                HealthReport.component(rabbitUp ? HealthStatus.UP : HealthStatus.DOWN, rabbitDetails)
        );
    }

    private <T> T safe(java.util.concurrent.Callable<T> c) {
        try { return c.call(); } catch (Exception e) { return null; }
    }
}
