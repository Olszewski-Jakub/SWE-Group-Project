package ie.universityofgalway.groupnine.delivery.rest.dev;

import ie.universityofgalway.groupnine.security.jwt.JwtService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import ie.universityofgalway.groupnine.domain.user.Role;
import java.util.Map;
import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;

/**
 * Development-only endpoint to mint a JWT for manual testing.
 * <p>
 * Available only when the active Spring profile is {@code dev} or {@code local}.
 */
@RestController
@RequestMapping("/dev")
@PublicEndpoint
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
    public ResponseEntity<Map<String, String>> devToken(@RequestParam(name = "roles", required = false) String rolesCsv) {
        java.util.List<String> roles = rolesCsv == null || rolesCsv.isBlank()
                ? List.of(Role.CUSTOMER.name())
                : java.util.Arrays.stream(rolesCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(String::toUpperCase)
                    .toList();
        String token = jwtService.createAccessToken("user-123", roles, Map.of());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
