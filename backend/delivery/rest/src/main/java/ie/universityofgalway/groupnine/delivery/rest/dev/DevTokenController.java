package ie.universityofgalway.groupnine.delivery.rest.dev;

import ie.universityofgalway.groupnine.security.jwt.JwtService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Development-only endpoint to mint a JWT for manual testing.
 * <p>
 * Available only when the active Spring profile is {@code dev} or {@code local}.
 */
@RestController
@Profile({"dev", "local"})
@RequestMapping("/dev")
public class DevTokenController {

    private final JwtService jwtService;

    public DevTokenController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Issues a simple access token with a fixed subject and role.
     *
     * @return JSON body containing a signed JWT under the {@code token} key
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> devToken() {
        String token = jwtService.createAccessToken("user-123", List.of("CUSTOMER"), Map.of());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
