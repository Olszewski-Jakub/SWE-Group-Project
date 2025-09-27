package ie.universityofgalway.groupnine.infrastructure.common.adapter;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemClockAdapterTest {

    @Test
    void now_is_close_to_system_time() {
        SystemClockAdapter adapter = new SystemClockAdapter();
        Instant before = Instant.now().minusSeconds(1);
        Instant now = adapter.now();
        Instant after = Instant.now().plusSeconds(1);
        assertTrue(!now.isBefore(before) && !now.isAfter(after));
    }
}

