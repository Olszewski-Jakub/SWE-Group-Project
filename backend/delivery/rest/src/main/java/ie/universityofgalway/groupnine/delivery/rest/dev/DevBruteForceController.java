package ie.universityofgalway.groupnine.delivery.rest.dev;

import ie.universityofgalway.groupnine.service.auth.port.BruteForceGuardPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Development endpoint to expose which {@link BruteForceGuardPort} implementation is active.
 */
@RestController
@RequestMapping("/dev/bruteforce")
public class DevBruteForceController {
    private final BruteForceGuardPort guard;

    public DevBruteForceController(BruteForceGuardPort guard) {
        this.guard = guard;
    }

    @GetMapping("/which")
    public ResponseEntity<Map<String, String>> which() {
        return ResponseEntity.ok(Map.of(
                "implementation", guard.getClass().getName()
        ));
    }
}

