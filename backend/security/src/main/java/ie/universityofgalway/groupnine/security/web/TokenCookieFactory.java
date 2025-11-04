package ie.universityofgalway.groupnine.security.web;

import ie.universityofgalway.groupnine.security.config.props.AppSecurityProps;
import ie.universityofgalway.groupnine.security.config.props.AuthProps;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import static ie.universityofgalway.groupnine.util.Routes.AUTH;

@Component
public class TokenCookieFactory {
    private final AuthProps authProps;
    private final AppSecurityProps appSecurityProps;

    public TokenCookieFactory(AuthProps authProps, AppSecurityProps appSecurityProps) {
        this.authProps = authProps;
        this.appSecurityProps = appSecurityProps;
    }

    public ResponseCookie buildRefreshCookie(String token) {
        long maxAge = java.time.Duration.ofDays(authProps.getRefreshTtlDays()).getSeconds();
        return ResponseCookie.from(authProps.getRefreshCookieName(), token)
                .httpOnly(true)
                .secure(authProps.isCookieSecure())
                .sameSite(authProps.getCookieSameSite())
                .path(AUTH + "/refresh")
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie buildAccessCookie(String token) {
        int ttl = (int) appSecurityProps.getJwt().getAccessTokenTtl().getSeconds();
        return ResponseCookie.from("accessToken", token)
                .httpOnly(false)
                .secure(authProps.isCookieSecure())
                .sameSite(authProps.getCookieSameSite())
                .path("/")
                .maxAge(ttl)
                .build();
    }
}

