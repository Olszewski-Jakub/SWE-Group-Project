package ie.universityofgalway.groupnine.security;

import com.nimbusds.jwt.JWTClaimsSet;
import ie.universityofgalway.groupnine.security.config.props.AppSecurityProps;
import ie.universityofgalway.groupnine.security.jwt.JwtException;
import ie.universityofgalway.groupnine.security.jwt.JwtService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Map;

class JwtServiceTest {

    private AppSecurityProps props;
    private Clock clock;
    private JwtService service;

    @BeforeEach
    void setUp() {
        props = new AppSecurityProps();
        AppSecurityProps.Jwt jwt = new AppSecurityProps.Jwt();
        // 32 bytes Base64 secret
        String secret = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        jwt.setHmacSecret(secret);
        jwt.setAuthoritiesClaim("roles");
        jwt.setIssuer("https://issuer");
        jwt.setAccessTokenTtl(Duration.ofMinutes(30));
        props.setJwt(jwt);

        clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
        service = new JwtService(props, clock);
    }

    @Test
    void createAndValidate_roundtrip() {
        String token = service.createAccessToken("user-1", List.of("ADMIN", "USER"), Map.of("x", 1));
        JWTClaimsSet claims = service.validate(token);

        Assertions.assertThat(claims.getSubject()).isEqualTo("user-1");
        Assertions.assertThat(claims.getIssuer()).isEqualTo("https://issuer");
        Object roles = claims.getClaims().get("roles");
        Assertions.assertThat(roles).isInstanceOf(List.class);
        @SuppressWarnings("unchecked") List<Object> rolesList = (List<Object>) roles;
        Assertions.assertThat(rolesList.stream().map(String::valueOf).toList())
                .containsExactlyInAnyOrder("ADMIN", "USER");
        Object x = claims.getClaims().get("x");
        Assertions.assertThat(x).isInstanceOf(Number.class);
        Assertions.assertThat(((Number) x).longValue()).isEqualTo(1L);
    }

    @Test
    void validate_throws_on_bad_issuer() {
        String token = service.createAccessToken("user-1", List.of(), Map.of());
        // Change expected issuer
        props.getJwt().setIssuer("https://other");
        Assertions.assertThatThrownBy(() -> service.validate(token))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid issuer");
    }

    @Test
    void validate_throws_on_expired() {
        props.getJwt().setAccessTokenTtl(Duration.ZERO);
        String token = service.createAccessToken("user-1", List.of(), Map.of());
        Assertions.assertThatThrownBy(() -> service.validate(token))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    void too_short_secret_is_rejected() {
        props.getJwt().setHmacSecret("short");
        JwtService s = new JwtService(props, clock);
        Assertions.assertThatThrownBy(() -> s.createAccessToken("u", List.of(), Map.of()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("HMAC secret too short");
    }
}
