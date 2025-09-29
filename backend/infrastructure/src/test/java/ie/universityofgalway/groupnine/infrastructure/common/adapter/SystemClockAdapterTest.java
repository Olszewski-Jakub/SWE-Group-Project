package ie.universityofgalway.groupnine.infrastructure.common.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SystemClockAdapterTest {
    @Test
    void nowReturnsCurrentInstant() {
        SystemClockAdapter clock = new SystemClockAdapter();
        assertNotNull(clock.now());
    }
}

