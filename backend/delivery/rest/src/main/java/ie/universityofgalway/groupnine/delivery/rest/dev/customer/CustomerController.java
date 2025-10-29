package ie.universityofgalway.groupnine.delivery.rest.dev.customer;

import ie.universityofgalway.groupnine.delivery.rest.util.Routes;
import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.domain.security.RequireRoles;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(Routes.CUSTOMER)
@RequireRoles({Role.CUSTOMER})
public class CustomerController {
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "customer-ok"));
    }
}
