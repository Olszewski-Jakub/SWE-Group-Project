package ie.universityofgalway.groupnine.security;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.security")
@Validated
public class RouteProperties {

    private Routes routes = new Routes();
    private Jwt jwt = new Jwt();

    public Routes getRoutes() { return routes; }
    public Jwt getJwt() { return jwt; }

    @Validated
    public static class Routes {
        private List<String> permitAll = List.of( "/api/health");
        private List<String> authenticated = List.of("/api/**");
        /** Map of ROLE -> ant patterns */
        private Map<String, List<String>> roles = Map.of();

        public List<String> getPermitAll() { return permitAll; }
        public void setPermitAll(List<String> permitAll) { this.permitAll = permitAll; }
        public List<String> getAuthenticated() { return authenticated; }
        public void setAuthenticated(List<String> authenticated) { this.authenticated = authenticated; }
        public Map<String, List<String>> getRoles() { return roles; }
        public void setRoles(Map<String, List<String>> roles) { this.roles = roles; }
    }

    @Validated
    public static class Jwt {
        /** Optional issuer to validate against */
        private String issuer;
        /** HMAC secret (for HS256). Recommend providing via env var in deploy/local. */
        @NotNull
        private String hmacSecret = "dev-secret-change-me";
        /** Claim name that contains roles/authorities array */
        private String authoritiesClaim = "roles";

        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getHmacSecret() { return hmacSecret; }
        public void setHmacSecret(String hmacSecret) { this.hmacSecret = hmacSecret; }
        public String getAuthoritiesClaim() { return authoritiesClaim; }
        public void setAuthoritiesClaim(String authoritiesClaim) { this.authoritiesClaim = authoritiesClaim; }
    }
}

