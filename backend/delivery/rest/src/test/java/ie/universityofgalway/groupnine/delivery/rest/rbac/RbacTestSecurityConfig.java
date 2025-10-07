package ie.universityofgalway.groupnine.delivery.rest.rbac;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.security.web.AuthGuardInterceptor;
import ie.universityofgalway.groupnine.security.web.JsonAuthHandlers;
import ie.universityofgalway.groupnine.security.web.RequireRolesInterceptor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Test-time security config that registers the same MVC interceptors used in production,
 * allowing RBAC assertions (401/403/200) without loading the full Spring Security filter chain.
 */
@TestConfiguration
public class RbacTestSecurityConfig {
    @Bean
    JsonAuthHandlers jsonAuthHandlers(ObjectMapper mapper) {
        return new JsonAuthHandlers(mapper);
    }

    @Bean
    AuthGuardInterceptor authGuard(JsonAuthHandlers handlers) {
        return new AuthGuardInterceptor(handlers);
    }

    @Bean
    RequireRolesInterceptor requireRoles(JsonAuthHandlers handlers) {
        return new RequireRolesInterceptor(handlers);
    }

    @Bean
    WebMvcConfigurer rbacInterceptors(AuthGuardInterceptor auth, RequireRolesInterceptor roles) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(auth).order(-10);
                registry.addInterceptor(roles).order(0);
            }
        };
    }
}
