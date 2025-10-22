package ie.universityofgalway.groupnine.security.jwt;

import ie.universityofgalway.groupnine.domain.user.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility to convert a JWT roles/authorities claim into Spring Security authorities.
 * <p>
 * Role hierarchy expansion is delegated to the domain {@link Role} type to centralize
 * business rules and avoid duplication across layers.
 */
public final class Authorities {

    private Authorities() {
    }

    public static Collection<GrantedAuthority> fromClaim(Object rawClaim) {
        if (!(rawClaim instanceof List<?> list)) {
            return List.of();
        }
        // Normalize to bare role names (no ROLE_ prefix), uppercase -> map to domain Role
        List<Role> roles = list.stream()
                .map(Objects::toString)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .map(String::toUpperCase)
                .map(Authorities::toRoleSafe)
                .filter(Objects::nonNull)
                .toList();

            // Expand with domain hierarchy
        Set<Role> expanded = Role.expandHierarchy(roles);

        return expanded.stream()
                .map(Role::name)
                .map(name -> "ROLE_" + name)
                .sorted()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    private static Role toRoleSafe(String name) {
        try {
            return Role.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
