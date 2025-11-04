package ie.universityofgalway.groupnine.service.auth.port;

import java.util.List;

public interface GoogleOAuthConfigPort {
    String getAuthorizationUri();
    String getTokenUri();
    String getUserInfoUri();
    String getClientId();
    String getClientSecret();
    String getRedirectUri();
    List<String> getScopes();
    List<String> getAllowedRedirects();
}

