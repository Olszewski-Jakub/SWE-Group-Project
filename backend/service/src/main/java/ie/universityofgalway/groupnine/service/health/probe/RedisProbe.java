package ie.universityofgalway.groupnine.service.health.probe;

import java.util.Map;

/**
 * Output port for probing Redis health and obtaining basic runtime stats.
 * <p>
 * Implementations should be lightweight and safe to call on a health endpoint.
 */
public interface RedisProbe {
    /** @return true if the server responds to PING. */
    boolean ping();
    /** @return number of keys in the current DB (may be null on error). */
    Long dbSize();
    /** @return selected INFO sections mapped to simple keys (e.g., version, mode, memory). */
    Map<String, Object> serverInfo();
}
