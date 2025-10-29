package ie.universityofgalway.groupnine.delivery.rest.util.auth;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.security.jwt.JwtClaims;
import ie.universityofgalway.groupnine.security.jwt.JwtService;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessTokenUserResolverTest {

    @Test
    void resolve_returnsUserWhenValidBearerToken() {
        JwtService jwt = mock(JwtService.class);
        UserRepositoryPort users = mock(UserRepositoryPort.class);
        AccessTokenUserResolver resolver = new AccessTokenUserResolver(jwt, users);

        UUID uid = UUID.randomUUID();
        JwtClaims claims = new JwtClaims(uid.toString(),"iss", Instant.now(), Instant.now().plusSeconds(60), java.util.List.of("ADMIN"), Map.of());
        when(jwt.validate("tok")).thenReturn(claims);
        when(users.findById(UserId.of(uid))).thenReturn(Optional.of(new User(UserId.of(uid), Email.of("u@e.com"), "f","l", UserStatus.ACTIVE, true, null, Instant.now(), Instant.now())));
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Authorization")).thenReturn("Bearer tok");

        Optional<User> user = resolver.resolve(req);
        assertTrue(user.isPresent());
        assertEquals(uid, user.get().getId().value());
    }

    @Test
    void resolve_emptyWhenNoHeaderOrInvalid() {
        JwtService jwt = mock(JwtService.class);
        UserRepositoryPort users = mock(UserRepositoryPort.class);
        AccessTokenUserResolver resolver = new AccessTokenUserResolver(jwt, users);
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("Authorization")).thenReturn(null);
        assertTrue(resolver.resolve(req).isEmpty());
    }
}
