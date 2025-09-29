package ie.universityofgalway.groupnine.security.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties({
        ie.universityofgalway.groupnine.security.config.props.RouteProperties.class,
        ie.universityofgalway.groupnine.security.config.props.AppSecurityProps.class,
        ie.universityofgalway.groupnine.security.config.props.AuthProps.class
})
public class SecurityConfig {

    private static AntPathRequestMatcher ant(String pattern) {
        return new AntPathRequestMatcher(pattern);
    }

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
            config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(java.util.List.of("*"));
            config.setAllowCredentials(true);
            return config;
        }));
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(registry -> {
            // 1) static & actuator
            registry.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
            registry.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            registry.requestMatchers(ant("/actuator/info")).permitAll();
            // 2) public routes
            // Always allow health endpoint by default
            if (props.getRoutes() != null && props.getRoutes().getPublicRoutes() != null) {
                for (ie.universityofgalway.groupnine.security.config.props.AppSecurityProps.PublicRoute r : props.getRoutes().getPublicRoutes()) {
                    registry.requestMatchers(ant(r.getPattern())).permitAll();
                }
            }

            // 3) secure routes
            if (props.getRoutes() != null && props.getRoutes().getSecureRoutes() != null) {
                props.getRoutes().getSecureRoutes().forEach(r -> {
                    if (r.getRole() != null && !r.getRole().isBlank()) {
                        registry.requestMatchers(ant(r.getPattern())).hasRole(r.getRole());
                    } else {
                        registry.requestMatchers(ant(r.getPattern())).authenticated();
                    }
                });
            }

            // 4) terminal rule
            registry.anyRequest().denyAll();
        });

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

    // Keep JwtDecoder bean for tests and potential future use
    @Bean
    public JwtDecoder jwtDecoder(ie.universityofgalway.groupnine.security.config.props.RouteProperties props) {
        byte[] secret = props.getJwt().getHmacSecret().getBytes(StandardCharsets.UTF_8);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, "HmacSHA256")).build();
        if (props.getJwt().getIssuer() != null && !props.getJwt().getIssuer().isBlank()) {
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.getJwt().getIssuer()));
        }
        return decoder;
    }

}
