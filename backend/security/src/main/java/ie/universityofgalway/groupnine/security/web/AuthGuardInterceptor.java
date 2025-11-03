package ie.universityofgalway.groupnine.security.web;

import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;
import ie.universityofgalway.groupnine.security.config.props.AppSecurityProps;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
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

    private static final AppLogger LOG = AppLogger.get(AuthGuardInterceptor.class);
    private final JsonAuthHandlers handlers;
    private final AppSecurityProps props;
    private final org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();

    public AuthGuardInterceptor(JsonAuthHandlers handlers, AppSecurityProps props) {
        this.handlers = handlers;
        this.props = props;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        boolean annotatedPublic = isPublic(hm);
        boolean configuredPublic = isConfiguredPublic(request);
        if (annotatedPublic || configuredPublic) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("authguard_public", "uri", request.getRequestURI(), "annotated", String.valueOf(annotatedPublic), "configured", String.valueOf(configuredPublic));
            }
            return true; // public endpoint
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = (auth instanceof AnonymousAuthenticationToken)
                || (auth != null && "anonymousUser".equals(String.valueOf(auth.getPrincipal())));
        if (auth == null || !auth.isAuthenticated() || isAnonymous) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("authguard_unauthenticated", "uri", request.getRequestURI());
            }
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

    private boolean isConfiguredPublic(HttpServletRequest request) {
        if (props == null || props.getRoutes() == null || props.getRoutes().getPublicRoutes() == null) return false;
        String uri = request.getRequestURI();
        for (AppSecurityProps.PublicRoute pr : props.getRoutes().getPublicRoutes()) {
            if (pr != null && pr.getPattern() != null && pathMatcher.match(pr.getPattern(), uri)) {
                return true;
            }
        }
        return false;
    }
}
