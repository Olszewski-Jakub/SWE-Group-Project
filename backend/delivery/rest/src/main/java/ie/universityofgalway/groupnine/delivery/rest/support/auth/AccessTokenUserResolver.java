package ie.universityofgalway.groupnine.delivery.rest.support.auth;

import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.security.jwt.JwtClaims;
import ie.universityofgalway.groupnine.security.jwt.JwtService;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccessTokenUserResolver {
    private final JwtService jwtService;
    private final UserRepositoryPort userRepository;

    @Autowired
    public AccessTokenUserResolver(JwtService jwtService, UserRepositoryPort userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public Optional<User> resolve(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = auth.substring("Bearer ".length());
        JwtClaims claims = jwtService.validate(token);
        String sub = claims.getSubject();
        if (sub == null || sub.isBlank()) return Optional.empty();
        try {
            UUID id = UUID.fromString(sub);
            return userRepository.findById(UserId.of(id));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public User requireUser(HttpServletRequest request) {
        return resolve(request).orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }
}
