package ie.universityofgalway.groupnine.infrastructure.health;


import ie.universityofgalway.groupnine.service.health.probe.DatabaseProbe;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * JDBC-backed database probe that executes a trivial query for liveness and
 * exposes JDBC metadata and HikariCP pool metrics for diagnostics.
 */
@Component
public class DatabaseProbeImpl implements DatabaseProbe {
    private final DataSource dataSource;

    public DatabaseProbeImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Returns true if a simple "SELECT 1" query succeeds. */
    @Override
    public boolean pingDatabase() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next(); // true if DB is reachable
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Database details including product/version and (when available) Hikari pool statistics.
     * Values are best-effort and may be partially populated depending on the runtime environment.
     */
    @Override
    public Map<String, Object> details() {
        Map<String, Object> map = new HashMap<>();
        try (Connection c = dataSource.getConnection()) {
            var md = c.getMetaData();
            map.put("product", md.getDatabaseProductName());
            map.put("version", md.getDatabaseProductVersion());
        } catch (Exception ignored) {}

        // HikariCP pool metrics when available
        try {
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource hikari) {
                var poolMx = hikari.getHikariPoolMXBean();
                if (poolMx != null) {
                    map.put("active", poolMx.getActiveConnections());
                    map.put("idle", poolMx.getIdleConnections());
                    map.put("total", poolMx.getTotalConnections());
                    map.put("threadsAwaiting", poolMx.getThreadsAwaitingConnection());
                }
                var cfg = hikari.getHikariConfigMXBean();
                if (cfg != null) {
                    map.put("maxPoolSize", cfg.getMaximumPoolSize());
                    map.put("minIdle", cfg.getMinimumIdle());
                    map.put("poolName", cfg.getPoolName());
                }
            }
        } catch (Throwable ignored) {}
        return map;
    }
}
