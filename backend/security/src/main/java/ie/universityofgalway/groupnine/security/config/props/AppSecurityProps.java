package ie.universityofgalway.groupnine.security.config.props;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Typed application security properties bound from app.security.*
 */
@ConfigurationProperties(prefix = "app.security")
@Validated
public class AppSecurityProps {

    @Valid
    private Routes routes = new Routes();

    @Valid
    private Jwt jwt = new Jwt();

    public Routes getRoutes() {
        return routes;
    }

    public void setRoutes(Routes routes) {
        this.routes = routes;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    /**
     * HTTP routing rules: public routes and secure routes.
     */
    @Validated
    public static class Routes {
        @Valid
        private List<PublicRoute> publicRoutes = new ArrayList<>();
        @Valid
        private List<SecureRoute> secureRoutes = new ArrayList<>();

        public List<PublicRoute> getPublicRoutes() {
            return publicRoutes;
        }

        public void setPublicRoutes(List<PublicRoute> publicRoutes) {
            this.publicRoutes = publicRoutes;
        }

        public List<SecureRoute> getSecureRoutes() {
            return secureRoutes;
        }

        public void setSecureRoutes(List<SecureRoute> secureRoutes) {
            this.secureRoutes = secureRoutes;
        }
    }

    /**
     * Publicly accessible route (no authentication required).
     */
    @Validated
    public static class PublicRoute {
        @NotBlank
        private String pattern;

        public PublicRoute() {
        }

        public PublicRoute(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }

    /**
     * Secure route: requires authentication; if role provided, requires that role.
     */
    @Validated
    public static class SecureRoute {
        @NotBlank
        private String pattern;
        private String role; // optional

        public SecureRoute() {
        }

        public SecureRoute(String pattern, String role) {
            this.pattern = pattern;
            this.role = role;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    /**
     * JWT settings used for token creation/validation.
     */
    @Validated
    public static class Jwt {
        /**
         * Optional issuer embedded into tokens and validated on input.
         */
        private String issuer;
        /**
         * HS256 secret. May be plain text or Base64
         */
        @NotNull
        private String hmacSecret = "dev-secret-change-me";
        /**
         * Claim name that contains roles/authorities array
         */
        @NotBlank
        private String authoritiesClaim = "roles";
        /**
         * Access token time-to-live (default 60 minutes).
         */
        @NotNull
        private java.time.Duration accessTokenTtl = java.time.Duration.ofMinutes(60);

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getHmacSecret() {
            return hmacSecret;
        }

        public void setHmacSecret(String hmacSecret) {
            this.hmacSecret = hmacSecret;
        }

        public String getAuthoritiesClaim() {
            return authoritiesClaim;
        }

        public void setAuthoritiesClaim(String authoritiesClaim) {
            this.authoritiesClaim = authoritiesClaim;
        }

        public java.time.Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(java.time.Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }
    }
}
