package ie.universityofgalway.groupnine.infrastructure.health;


import ie.universityofgalway.groupnine.service.health.DatabaseProbe;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component
public class DatabaseProbeImpl implements DatabaseProbe {
    private final DataSource dataSource;

    public DatabaseProbeImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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
}
