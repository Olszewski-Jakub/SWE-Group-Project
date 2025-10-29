package ie.universityofgalway.groupnine.infrastructure.health;

import ie.universityofgalway.groupnine.service.health.probe.RedisProbe;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Redis probe backed by {@link org.springframework.data.redis.core.StringRedisTemplate}.
 * <p>
 * Uses low-level {@link org.springframework.data.redis.connection.RedisConnection} to avoid
 * ambiguous lambda overloads and collects lightweight INFO sections for diagnostics.
 */
@Component
public class RedisProbeImpl implements RedisProbe {
    private final StringRedisTemplate redis;

    public RedisProbeImpl(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Executes a PING command and expects a PONG.
     */
    @Override
    public boolean ping() {
        try (RedisConnection c = redis.getRequiredConnectionFactory().getConnection()) {
            String pong = c.ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the key count of the current database (DBSIZE). */
    @Override
    public Long dbSize() {
        try (RedisConnection c = redis.getRequiredConnectionFactory().getConnection()) {
            return c.serverCommands().dbSize();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns selected INFO sections projected into a simple map (server, memory, clients, stats, replication, keyspace).
     */
    @Override
    public Map<String, Object> serverInfo() {
        Map<String, Object> map = new HashMap<>();
        try (RedisConnection c = redis.getRequiredConnectionFactory().getConnection()) {
            Properties server = c.serverCommands().info("server");
            if (server != null) {
                Object ver = server.get("redis_version");
                if (ver != null) map.put("version", String.valueOf(ver));
                Object mode = server.get("redis_mode");
                if (mode != null) map.put("mode", String.valueOf(mode));
                Object os = server.get("os");
                if (os != null) map.put("os", String.valueOf(os));
                Object uptime = server.get("uptime_in_seconds");
                if (uptime != null) map.put("uptimeSeconds", String.valueOf(uptime));
            }

            Properties memory = c.serverCommands().info("memory");
            if (memory != null) {
                putIfPresent(memory, map, "used_memory_human", "usedMemory");
                putIfPresent(memory, map, "maxmemory_human", "maxMemory");
            }
            Properties clients = c.serverCommands().info("clients");
            if (clients != null) {
                putIfPresent(clients, map, "connected_clients", "connectedClients");
            }
            Properties stats = c.serverCommands().info("stats");
            if (stats != null) {
                putIfPresent(stats, map, "instantaneous_ops_per_sec", "opsPerSec");
            }
            Properties replication = c.serverCommands().info("replication");
            if (replication != null) {
                putIfPresent(replication, map, "role", "role");
                putIfPresent(replication, map, "connected_slaves", "replicaCount");
            }
            Properties keyspace = c.serverCommands().info("keyspace");
            if (keyspace != null) {
                Object db0 = keyspace.get("db0");
                if (db0 != null) map.put("db0", String.valueOf(db0));
            }
        } catch (Exception ignored) {}
        return map;
    }

    private static void putIfPresent(Properties src, Map<String, Object> dst, String key, String as) {
        Object v = src.get(key);
        if (v != null) dst.put(as, String.valueOf(v));
    }
}
