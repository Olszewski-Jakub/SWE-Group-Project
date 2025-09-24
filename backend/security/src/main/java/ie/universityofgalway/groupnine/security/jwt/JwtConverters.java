package ie.universityofgalway.groupnine.security.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.Objects;

public final class JwtConverters {

    public static JwtAuthenticationConverter jwtAuthenticationConverter(String authoritiesClaim) {
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(new JwtAuthoritiesConverter(authoritiesClaim));
        return c;
    }

    private record JwtAuthoritiesConverter(String claim) implements Converter<Jwt, Collection<GrantedAuthority>> {
            private JwtAuthoritiesConverter(String claim) {
                this.claim = Objects.requireNonNull(claim);
            }

            @Override
            public Collection<GrantedAuthority> convert(Jwt jwt) {
                Object raw = jwt.getClaims().get(claim);
            return Authorities.fromClaim(raw);
        }
    }
}
