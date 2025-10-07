package ie.universityofgalway.groupnine.delivery.rest.admin;

import ie.universityofgalway.groupnine.delivery.rest.support.Routes;
import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.domain.security.RequireRoles;
import ie.universityofgalway.groupnine.service.auth.usecase.AssignRoleToUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.GetUserRolesUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RevokeRoleFromUserUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(Routes.ADMIN + "/users")
@RequireRoles({Role.ADMIN})
public class AdminRoleController {
    private final AssignRoleToUserUseCase assign;
    private final RevokeRoleFromUserUseCase revoke;
    private final GetUserRolesUseCase getRoles;

    public AdminRoleController(AssignRoleToUserUseCase assign,
                               RevokeRoleFromUserUseCase revoke,
                               GetUserRolesUseCase getRoles) {
        this.assign = assign;
        this.revoke = revoke;
        this.getRoles = getRoles;
    }

    @GetMapping(path = "{userId}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> list(@PathVariable String userId) {
        var res = getRoles.execute(userId);
        return ResponseEntity.ok(Map.of("userId", res.userId(), "roles", res.roles()));
    }

    @PostMapping(path = "{userId}/roles/{role}")
    public ResponseEntity<Map<String, Object>> assign(@PathVariable String userId, @PathVariable String role) {
        var res = assign.execute(userId, role);
        return ResponseEntity.ok(Map.of("userId", res.userId(), "roles", res.roles()));
    }

    @DeleteMapping(path = "{userId}/roles/{role}")
    public ResponseEntity<Map<String, Object>> revoke(@PathVariable String userId, @PathVariable String role) {
        var res = revoke.execute(userId, role);
        return ResponseEntity.ok(Map.of("userId", res.userId(), "roles", res.roles()));
    }
}
