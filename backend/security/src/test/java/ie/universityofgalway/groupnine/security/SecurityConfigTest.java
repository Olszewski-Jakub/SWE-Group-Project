package ie.universityofgalway.groupnine.security;

import ie.universityofgalway.groupnine.security.config.SecurityConfig;
import ie.universityofgalway.groupnine.security.config.props.RouteProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class SecurityConfigTest {

    @Test
    void jwtDecoderUsesDefaultSecretAndNoIssuer() {
        SecurityConfig config = new SecurityConfig();
        RouteProperties props = new RouteProperties();
        props.getJwt().setHmacSecret("my-secret");
        props.getJwt().setIssuer(null);

        JwtDecoder decoder = config.jwtDecoder(props);
        Assertions.assertNotNull(decoder);
    }

    @Test
    void jwtDecoderUsesIssuerWhenProvided() {
        SecurityConfig config = new SecurityConfig();
        RouteProperties props = new RouteProperties();
        props.getJwt().setHmacSecret("my-secret");
        props.getJwt().setIssuer("https://issuer.example.com");

        JwtDecoder decoder = config.jwtDecoder(props);
        Assertions.assertNotNull(decoder);
    }
}
