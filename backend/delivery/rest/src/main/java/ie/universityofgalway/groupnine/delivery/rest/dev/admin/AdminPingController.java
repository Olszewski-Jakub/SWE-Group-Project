package ie.universityofgalway.groupnine.delivery.rest.dev.admin;

import ie.universityofgalway.groupnine.util.Routes;
import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.domain.security.RequireRoles;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(Routes.ADMIN)
@RequireRoles({Role.ADMIN})
public class AdminPingController {
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "admin-ok"));
    }
}
