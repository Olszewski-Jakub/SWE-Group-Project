package ie.universityofgalway.groupnine.security.web;

import ie.universityofgalway.groupnine.security.config.props.AppSecurityProps;
import ie.universityofgalway.groupnine.security.jwt.Authorities;
import ie.universityofgalway.groupnine.security.jwt.JwtException;
import ie.universityofgalway.groupnine.security.jwt.JwtService;
import ie.universityofgalway.groupnine.security.jwt.JwtClaims;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

/**
 * Servlet filter that authenticates requests using a Bearer JWT.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final AppLogger LOG = AppLogger.get(JwtAuthFilter.class);
    private final JwtService jwtService;
    private final AppSecurityProps props;
    private final JsonAuthHandlers handlers;

    public JwtAuthFilter(JwtService jwtService, AppSecurityProps props, JsonAuthHandlers handlers) {
        this.jwtService = jwtService;
        this.props = props;
        this.handlers = handlers;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                JwtClaims claims = jwtService.validate(token);
                Authentication authentication = toAuthentication(token, claims);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                LOG.info(
                        "jwt_authenticated",
                        "sub", claims.getSubject() == null ? "" : claims.getSubject(),
                        "authorities_count", authentication.getAuthorities() == null ? 0 : authentication.getAuthorities().size()
                );
            } catch (JwtException ex) {
                // Return JSON 401
                LOG.warn("jwt_auth_failed", "reason", ex.getMessage());
                handlers.authenticationEntryPoint().commence(request, response, new AuthenticationException(ex.getMessage()) {
                });
                return;
            }
        } else {
            LOG.debug("no_bearer_token");
        }

        filterChain.doFilter(request, response);
    }

    private Authentication toAuthentication(String token, JwtClaims claims) {
        String subject = claims.getSubject();
        Collection<? extends GrantedAuthority> authorities = extractAuthorities(claims);
        return new UsernamePasswordAuthenticationToken(subject, token, authorities);
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(JwtClaims claims) {
        Object raw = claims.getClaims().get(props.getJwt().getAuthoritiesClaim());
        return Authorities.fromClaim(raw);
    }
}
