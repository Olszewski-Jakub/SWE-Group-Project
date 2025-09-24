package ie.universityofgalway.groupnine.delivery.rest.dev;

import ie.universityofgalway.groupnine.security.jwt.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dev")
public class DevTokenController {

    private final JwtService jwtService;

    public DevTokenController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> devToken() {
        String token = jwtService.createAccessToken("user-123", List.of("CUSTOMER"), Map.of());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
