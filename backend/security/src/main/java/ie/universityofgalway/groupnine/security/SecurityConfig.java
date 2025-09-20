package ie.universityofgalway.groupnine.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(RouteProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RouteProperties props,
            JsonAuthHandlers handlers
    ) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(registry -> {
            // 1) static & actuator
            registry.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
            registry.requestMatchers(ant("/actuator/info")).permitAll();

            // 2) permit-all from YAML
            for (String p : safe(props.getRoutes().getPermitAll())) {
                registry.requestMatchers(ant(p)).permitAll();
            }

            // 3) role-based rules (specific first)
            for (Map.Entry<String, List<String>> e : safeMap(props.getRoutes().getRoles()).entrySet()) {
                String role = e.getKey();
                for (String p : safe(e.getValue())) {
                    registry.requestMatchers(ant(p)).hasRole(role);
                }
            }

            // 4) authenticated catch-alls
            for (String p : safe(props.getRoutes().getAuthenticated())) {
                registry.requestMatchers(ant(p)).authenticated();
            }

            // 5) finally, the terminal rule — must be LAST:
            registry.anyRequest().denyAll();
        });

        // Resource server (JWT)
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
                .jwtAuthenticationConverter(
                        JwtConverters.jwtAuthenticationConverter(props.getJwt().getAuthoritiesClaim())
                )
        ));


        // Add custom JSON error handling
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(handlers.authenticationEntryPoint())
                .accessDeniedHandler(handlers.accessDeniedHandler())
        );

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(RouteProperties props) {
        byte[] secret = props.getJwt().getHmacSecret().getBytes(StandardCharsets.UTF_8);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, "HmacSHA256")).build();
        if (props.getJwt().getIssuer() != null && !props.getJwt().getIssuer().isBlank()) {
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.getJwt().getIssuer()));
        }
        return decoder;
    }

    private static AntPathRequestMatcher ant(String pattern) {
        return new AntPathRequestMatcher(pattern);
    }

    private static List<String> safe(List<String> list) {
        return list == null ? List.of() : list;
    }

    private static Map<String, List<String>> safeMap(Map<String, List<String>> map) {
        return map == null ? Map.of() : map;
    }
}
