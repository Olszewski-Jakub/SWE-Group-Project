package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.auth.dto.MeResponse;
import ie.universityofgalway.groupnine.delivery.rest.util.Routes;
import ie.universityofgalway.groupnine.delivery.rest.util.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Authenticated session endpoint for retrieving current user details.
 *
 * GET /api/v1/auth/me
 * Returns basic user profile fields and roles derived from the active JWT.
 */
@RestController
@RequestMapping(value = Routes.AUTH)
public class AuthMeController {
    private final AccessTokenUserResolver userResolver;

    @Autowired
    public AuthMeController(AccessTokenUserResolver userResolver) {
        this.userResolver = userResolver;
    }

    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MeResponse> me(HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = extractRoles(authentication);
        MeResponse body = new MeResponse(
                user.getId().value().toString(),
                user.getEmail().value(),
                user.getFirstName(),
                user.getLastName(),
                roles
        );
        return ResponseEntity.ok(body);
    }

    private List<String> extractRoles(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .sorted()
                .collect(Collectors.toList());
    }
}
