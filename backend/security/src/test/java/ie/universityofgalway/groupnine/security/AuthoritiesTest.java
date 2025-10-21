package ie.universityofgalway.groupnine.security;

import ie.universityofgalway.groupnine.security.jwt.Authorities;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

class AuthoritiesTest {

    @Test
    void mapsStringsToRoleAuthorities() {
        var auths = Authorities.fromClaim(List.of("ADMIN", "ROLE_USER"));
        Assertions.assertThat(auths)
                .extracting(GrantedAuthority::getAuthority)
                .contains(
                        "ROLE_ADMIN",
                        "ROLE_MANAGER",
                        "ROLE_STAFF",
                        "ROLE_SUPPORT"
                );
    }

    @Test
    void returnsEmptyWhenClaimIsNotAList() {
        var auths = Authorities.fromClaim("ADMIN");
        Assertions.assertThat(auths).isEmpty();
    }
}
