package ie.universityofgalway.groupnine.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class JwtConverters {

    static JwtAuthenticationConverter jwtAuthenticationConverter(String authoritiesClaim) {
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(new JwtAuthoritiesConverter(authoritiesClaim));
        return c;
    }

    private static class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        private final String claim;

        JwtAuthoritiesConverter(String claim) {
            this.claim = Objects.requireNonNull(claim);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Object raw = jwt.getClaims().get(claim);
            if (raw instanceof List<?> list) {
                return list.stream()
                        .map(Objects::toString)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableList());
            }
            return List.of();
        }
    }
}
