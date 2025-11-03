package ie.universityofgalway.groupnine.security.config;

import ie.universityofgalway.groupnine.security.web.RequireRolesInterceptor;
import ie.universityofgalway.groupnine.security.web.AuthGuardInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Central security configuration.
 * <p>
 * Strategy:
 * - HTTP layer permits all requests through the filter chain while remaining stateless
 *   (JWT is still parsed by {@code JwtAuthFilter}).
 * - Authorization is enforced in the MVC layer via two interceptors:
 *   {@code AuthGuardInterceptor} requires authentication unless an endpoint is annotated
 *   with {@code @PublicEndpoint}; {@code RequireRolesInterceptor} enforces domain roles
 *   declared with {@code @RequireRoles}.
 * - CORS is open to all origins for development; CSRF disabled; sessions are stateless.
 */
@Configuration
@EnableConfigurationProperties({
        ie.universityofgalway.groupnine.security.config.props.AppSecurityProps.class,
        ie.universityofgalway.groupnine.security.config.props.AuthProps.class
})
public class SecurityConfig {

    // Use requestMatchers(String...) directly; avoid AntPathRequestMatcher (deprecated)

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ie.universityofgalway.groupnine.security.config.props.AppSecurityProps props,
            ie.universityofgalway.groupnine.security.web.JsonAuthHandlers handlers,
            ie.universityofgalway.groupnine.security.web.JwtAuthFilter jwtAuthFilter
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(java.util.List.of("*"));
            config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(java.util.List.of("*"));
            config.setAllowCredentials(true);
            return config;
        }));
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Path-level: allow all through; MVC interceptors enforce auth/roles
        http.authorizeHttpRequests(registry -> registry.anyRequest().permitAll());

        // Add JWT filter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Add custom JSON error handling
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(handlers.authenticationEntryPoint())
                .accessDeniedHandler(handlers.accessDeniedHandler())
        );

        return http.build();
    }

    @Bean
    java.time.Clock clock() {
        return java.time.Clock.systemUTC();
    }

    // Register MVC interceptor to enforce @RequireRoles on controllers without requiring AOP proxies
    @Bean
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer interceptorsConfigurer(
            AuthGuardInterceptor authGuard,
            RequireRolesInterceptor rolesInterceptor
    ) {
        return new org.springframework.web.servlet.config.annotation.WebMvcConfigurer() {
            @Override
            public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
                registry.addInterceptor(authGuard).order(-10);
                registry.addInterceptor(rolesInterceptor).order(0);
            }
        };
    }

    // Keep JwtDecoder bean for tests and potential future use
    @Bean
    public JwtDecoder jwtDecoder(ie.universityofgalway.groupnine.security.config.props.AppSecurityProps props) {
        byte[] secret = props.getJwt().getHmacSecret().getBytes(StandardCharsets.UTF_8);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, "HmacSHA256")).build();
        if (props.getJwt().getIssuer() != null && !props.getJwt().getIssuer().isBlank()) {
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.getJwt().getIssuer()));
        }
        return decoder;
    }

    // Compatibility overload for tests that still pass RouteProperties
    public JwtDecoder jwtDecoder(ie.universityofgalway.groupnine.security.config.props.RouteProperties routeProps) {
        ie.universityofgalway.groupnine.security.config.props.AppSecurityProps app = new ie.universityofgalway.groupnine.security.config.props.AppSecurityProps();
        var jwt = new ie.universityofgalway.groupnine.security.config.props.AppSecurityProps.Jwt();
        jwt.setHmacSecret(routeProps.getJwt().getHmacSecret());
        jwt.setIssuer(routeProps.getJwt().getIssuer());
        jwt.setAuthoritiesClaim(routeProps.getJwt().getAuthoritiesClaim());
        app.setJwt(jwt);
        return jwtDecoder(app);
    }

}
