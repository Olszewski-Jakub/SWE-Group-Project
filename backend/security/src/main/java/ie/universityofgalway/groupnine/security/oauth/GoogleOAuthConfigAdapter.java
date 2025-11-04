package ie.universityofgalway.groupnine.security.oauth;

import ie.universityofgalway.groupnine.security.config.props.GoogleOAuthProps;
import ie.universityofgalway.groupnine.service.auth.port.GoogleOAuthConfigPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoogleOAuthConfigAdapter implements GoogleOAuthConfigPort {
    private final GoogleOAuthProps props;

    public GoogleOAuthConfigAdapter(GoogleOAuthProps props) { this.props = props; }

    @Override public String getAuthorizationUri() { return props.getAuthorizationUri(); }
    @Override public String getTokenUri() { return props.getTokenUri(); }
    @Override public String getUserInfoUri() { return props.getUserInfoUri(); }
    @Override public String getClientId() { return props.getClientId(); }
    @Override public String getClientSecret() { return props.getClientSecret(); }
    @Override public String getRedirectUri() { return props.getRedirectUri(); }
    @Override public List<String> getScopes() { return props.getScopes(); }
    @Override public List<String> getAllowedRedirects() { return props.getAllowedRedirects(); }
}

