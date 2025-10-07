package ie.universityofgalway.groupnine.security.web;

import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Authentication guard applied at the MVC layer.
 * <p>
 * Behavior:
 * - If a handler (method or type) is annotated with {@link ie.universityofgalway.groupnine.domain.security.PublicEndpoint},
 *   the request proceeds without requiring authentication.
 * - Otherwise, the request must be authenticated with a non-anonymous {@link org.springframework.security.core.Authentication}.
 *   Anonymous and missing authentications yield a 401 response via {@link JsonAuthHandlers}.
 * <p>
 * This interceptor should execute before role enforcement to ensure the caller is authenticated
 * prior to evaluating {@code @RequireRoles}.
 */
@Component
public class AuthGuardInterceptor implements HandlerInterceptor {

    private final JsonAuthHandlers handlers;

    public AuthGuardInterceptor(JsonAuthHandlers handlers) {
        this.handlers = handlers;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        if (isPublic(hm)) {
            return true; // public endpoint
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = (auth instanceof AnonymousAuthenticationToken)
                || (auth != null && "anonymousUser".equals(String.valueOf(auth.getPrincipal())));
        if (auth == null || !auth.isAuthenticated() || isAnonymous) {
            handlers.authenticationEntryPoint().commence(request, response,
                    new AuthenticationCredentialsNotFoundException("Authentication required"));
            return false;
        }
        return true;
    }

    private boolean isPublic(HandlerMethod hm) {
        if (hm.getMethodAnnotation(PublicEndpoint.class) != null) return true;
        return hm.getBeanType().getAnnotation(PublicEndpoint.class) != null;
    }
}
