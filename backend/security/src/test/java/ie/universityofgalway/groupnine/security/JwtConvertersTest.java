package ie.universityofgalway.groupnine.security;

import ie.universityofgalway.groupnine.security.jwt.JwtConverters;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

class JwtConvertersTest {

    @Test
    void jwtAuthenticationConverterReturnsAuthoritiesFromClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of("ADMIN", "USER"))
                .build();

        var converter = JwtConverters.jwtAuthenticationConverter("roles");
        var auth = converter.convert(jwt);

        Assertions.assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains(
                        "ROLE_ADMIN",
                        "ROLE_MANAGER",
                        "ROLE_STAFF",
                        "ROLE_SUPPORT",
                        "ROLE_USER"
                );
    }

    @Test
    void jwtAuthenticationConverterReturnsEmptyAuthoritiesWhenClaimIsNotAList() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", "ADMIN")
                .build();

        var converter = JwtConverters.jwtAuthenticationConverter("roles");
        var auth = converter.convert(jwt);

        Assertions.assertThat(auth.getAuthorities()).isEmpty();
    }

    @Test
    void jwtAuthoritiesConverterPrefixesRoleIfMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of("ROLE_ADMIN", "USER"))
                .build();

        var converter = JwtConverters.jwtAuthenticationConverter("roles");
        var auth = converter.convert(jwt);

        Assertions.assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains(
                        "ROLE_ADMIN",
                        "ROLE_MANAGER",
                        "ROLE_STAFF",
                        "ROLE_SUPPORT",
                        "ROLE_USER"
                );
    }

    @Test
    void jwtAuthoritiesConverterHandlesEmptyListClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of())
                .build();

        var converter = JwtConverters.jwtAuthenticationConverter("roles");
        var auth = converter.convert(jwt);

        Assertions.assertThat(auth.getAuthorities()).isEmpty();
    }
}
