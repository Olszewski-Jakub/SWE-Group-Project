package ie.universityofgalway.groupnine.infrastructure.common.adapter;

import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SystemClockAdapter implements ClockPort {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
