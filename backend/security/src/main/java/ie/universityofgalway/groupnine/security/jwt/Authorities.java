package ie.universityofgalway.groupnine.security.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility to convert a JWT roles/authorities claim into Spring Security authorities.
 */
public final class Authorities {

    private Authorities() {}

    public static Collection<GrantedAuthority> fromClaim(Object rawClaim) {
        if (rawClaim instanceof List<?> list) {
            return list.stream()
                    .map(Objects::toString)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableList());
        }
        return List.of();
    }
}
