package ie.universityofgalway.groupnine.security;

import ie.universityofgalway.groupnine.security.config.props.RouteProperties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class RoutePropertiesTest {

    @Test
    void defaultPermitAllRoutesAreSet() {
        RouteProperties.Routes routes = new RouteProperties.Routes();
        Assertions.assertEquals(List.of("/api/health"), routes.getPermitAll());
    }

    @Test
    void defaultAuthenticatedRoutesAreSet() {
        RouteProperties.Routes routes = new RouteProperties.Routes();
        Assertions.assertEquals(List.of("/api/**"), routes.getAuthenticated());
    }

    @Test
    void defaultRolesMapIsEmpty() {
        RouteProperties.Routes routes = new RouteProperties.Routes();
        Assertions.assertTrue(routes.getRoles().isEmpty());
    }

    @Test
    void canSetPermitAllRoutes() {
        RouteProperties.Routes routes = new RouteProperties.Routes();
        routes.setPermitAll(List.of("/public", "/open"));
        Assertions.assertEquals(List.of("/public", "/open"), routes.getPermitAll());
    }

    @Test
    void canSetAuthenticatedRoutes() {
        RouteProperties.Routes routes = new RouteProperties.Routes();
        routes.setAuthenticated(List.of("/secure", "/private"));
        Assertions.assertEquals(List.of("/secure", "/private"), routes.getAuthenticated());
    }

    @Test
    void canSetRolesMap() {
        RouteProperties.Routes routes = new RouteProperties.Routes();
        Map<String, List<String>> roleMap = Map.of("ADMIN", List.of("/admin/**"), "USER", List.of("/user/**"));
        routes.setRoles(roleMap);
        Assertions.assertEquals(roleMap, routes.getRoles());
    }

    @Test
    void defaultJwtIssuerIsNull() {
        RouteProperties.Jwt jwt = new RouteProperties.Jwt();
        Assertions.assertNull(jwt.getIssuer());
    }

    @Test
    void defaultJwtHmacSecretIsSet() {
        RouteProperties.Jwt jwt = new RouteProperties.Jwt();
        Assertions.assertEquals("dev-secret-change-me", jwt.getHmacSecret());
    }

    @Test
    void defaultJwtAuthoritiesClaimIsSet() {
        RouteProperties.Jwt jwt = new RouteProperties.Jwt();
        Assertions.assertEquals("roles", jwt.getAuthoritiesClaim());
    }

    @Test
    void canSetJwtIssuer() {
        RouteProperties.Jwt jwt = new RouteProperties.Jwt();
        jwt.setIssuer("issuer-value");
        Assertions.assertEquals("issuer-value", jwt.getIssuer());
    }

    @Test
    void canSetJwtHmacSecret() {
        RouteProperties.Jwt jwt = new RouteProperties.Jwt();
        jwt.setHmacSecret("new-secret");
        Assertions.assertEquals("new-secret", jwt.getHmacSecret());
    }

    @Test
    void canSetJwtAuthoritiesClaim() {
        RouteProperties.Jwt jwt = new RouteProperties.Jwt();
        jwt.setAuthoritiesClaim("custom-claim");
        Assertions.assertEquals("custom-claim", jwt.getAuthoritiesClaim());
    }
}
