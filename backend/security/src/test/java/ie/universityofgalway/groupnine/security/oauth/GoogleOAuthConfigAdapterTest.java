package ie.universityofgalway.groupnine.security.oauth;

import ie.universityofgalway.groupnine.security.config.props.GoogleOAuthProps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoogleOAuthConfigAdapterTest {
    @Test
    void maps_props_to_port() {
        GoogleOAuthProps p = new GoogleOAuthProps();
        p.setAuthorizationUri("https://accounts.google.com/o/oauth2/v2/auth");
        p.setTokenUri("https://oauth2.googleapis.com/token");
        p.setUserInfoUri("https://openidconnect.googleapis.com/v1/userinfo");
        p.setClientId("cid");
        p.setClientSecret("sec");
        p.setRedirectUri("https://api.example.com/callback");
        p.setScopes(java.util.List.of("openid","email"));
        p.setAllowedRedirects(java.util.List.of("https://app.example.com"));

        GoogleOAuthConfigAdapter a = new GoogleOAuthConfigAdapter(p);
        assertEquals("cid", a.getClientId());
        assertEquals("https://api.example.com/callback", a.getRedirectUri());
        assertTrue(a.getScopes().contains("email"));
        assertTrue(a.getAllowedRedirects().get(0).startsWith("https://app.example.com"));
    }
}

