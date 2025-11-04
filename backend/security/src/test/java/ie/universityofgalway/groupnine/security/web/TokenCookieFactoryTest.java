package ie.universityofgalway.groupnine.security.web;

import ie.universityofgalway.groupnine.security.config.props.AppSecurityProps;
import ie.universityofgalway.groupnine.security.config.props.AuthProps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenCookieFactoryTest {
    @Test
    void builds_refresh_and_access_cookies() {
        AuthProps auth = new AuthProps();
        auth.setRefreshCookieName("refreshToken");
        auth.setRefreshTtlDays(14);
        auth.setCookieSecure(false);
        auth.setCookieSameSite("Lax");
        AppSecurityProps asp = new AppSecurityProps();
        AppSecurityProps.Jwt jwt = new AppSecurityProps.Jwt();
        jwt.setHmacSecret("0123456789abcdef0123456789abcdef01234567");
        jwt.setAccessTokenTtl(java.time.Duration.ofMinutes(15));
        asp.setJwt(jwt);

        TokenCookieFactory f = new TokenCookieFactory(auth, asp);
        var r = f.buildRefreshCookie("refr");
        assertTrue(r.toString().contains("refreshToken=refr"));
        var a = f.buildAccessCookie("acc");
        assertTrue(a.toString().contains("accessToken=acc"));
    }
}

