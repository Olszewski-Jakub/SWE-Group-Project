package ie.universityofgalway.groupnine.service.health.probe;

import java.util.Map;

/**
 * Output port for probing RabbitMQ health and obtaining broker details.
 * <p>
 * Implementations should avoid expensive calls such as management HTTP API unless explicitly configured.
 */
public interface RabbitProbe {
    /** @return true if a broker connection can be created and is open. */
    boolean ping();
    /** @return connection factory/broker properties useful for diagnostics (e.g., version, host, cache sizes). */
    Map<String, Object> serverInfo();
}
