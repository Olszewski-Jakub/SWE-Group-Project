package ie.universityofgalway.groupnine.security.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.oauth.google")
@Validated
public class GoogleOAuthProps {
    private String clientId;
    private String clientSecret;
    private String authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth";
    private String tokenUri = "https://oauth2.googleapis.com/token";
    private String userInfoUri = "https://openidconnect.googleapis.com/v1/userinfo";
    private String redirectUri;
    private List<String> scopes = new ArrayList<>(java.util.List.of("openid", "email", "profile"));
    private List<String> allowedRedirects = new ArrayList<>();

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getAuthorizationUri() { return authorizationUri; }
    public void setAuthorizationUri(String authorizationUri) { this.authorizationUri = authorizationUri; }

    public String getTokenUri() { return tokenUri; }
    public void setTokenUri(String tokenUri) { this.tokenUri = tokenUri; }

    public String getUserInfoUri() { return userInfoUri; }
    public void setUserInfoUri(String userInfoUri) { this.userInfoUri = userInfoUri; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }

    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }

    public List<String> getAllowedRedirects() { return allowedRedirects; }
    public void setAllowedRedirects(List<String> allowedRedirects) { this.allowedRedirects = allowedRedirects; }
}

