package ie.universityofgalway.groupnine.security.web;

import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.security.web.JsonAuthHandlers;
import ie.universityofgalway.groupnine.domain.security.RequireRoles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Role-based authorization interceptor.
 * <p>
 * Reads {@link ie.universityofgalway.groupnine.domain.security.RequireRoles} from the handler method or type
 * and ensures the authenticated principal has at least one of the required authorities.
 * Domain roles are translated to Spring Security authorities using the {@code ROLE_} prefix
 * (e.g. {@code Role.ADMIN} -> {@code ROLE_ADMIN}).
 * <p>
 * - Missing/anonymous authentication results in 401 (delegated to {@link JsonAuthHandlers}).
 * - Authenticated but insufficient privileges results in 403 (delegated to {@link JsonAuthHandlers}).
 * <p>
 * Placing the check at the MVC layer avoids reliance on AOP proxies and keeps REST controllers
 * decoupled from Spring Security annotations.
 */
@Component
public class RequireRolesInterceptor implements HandlerInterceptor {

    private final JsonAuthHandlers handlers;

    public RequireRolesInterceptor(JsonAuthHandlers handlers) {
        this.handlers = handlers;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        RequireRoles ann = resolveAnnotation(hm);
        if (ann == null) {
            return true; // no role requirement on this handler
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            handlers.authenticationEntryPoint().commence(request, response,
                    new AuthenticationCredentialsNotFoundException("No authenticated user"));
            return false;
        }

        Set<String> required = Arrays.stream(ann.value())
                .filter(Objects::nonNull)
                .map(Role::name)
                .map(r -> "ROLE_" + r)
                .collect(Collectors.toUnmodifiableSet());

        boolean has = auth.getAuthorities() != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(required::contains);

        if (!has) {
            handlers.accessDeniedHandler().handle(request, response, new AccessDeniedException("Forbidden"));
            return false;
        }
        return true;
    }

    private RequireRoles resolveAnnotation(HandlerMethod hm) {
        RequireRoles ann = hm.getMethodAnnotation(RequireRoles.class);
        if (ann != null) return ann;
        Class<?> beanType = hm.getBeanType();
        return beanType.getAnnotation(RequireRoles.class);
    }
}
