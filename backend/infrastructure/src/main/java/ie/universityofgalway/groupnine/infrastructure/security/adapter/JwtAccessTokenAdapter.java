package ie.universityofgalway.groupnine.infrastructure.security.adapter;

import ie.universityofgalway.groupnine.security.config.props.AppSecurityProps;
import ie.universityofgalway.groupnine.security.jwt.JwtService;
import ie.universityofgalway.groupnine.service.auth.port.JwtAccessTokenPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Adapter that delegates access token creation to the security JwtService.
 */
@Component
public class JwtAccessTokenAdapter implements JwtAccessTokenPort {
    private final JwtService jwtService;
    private final AppSecurityProps props;

    @Autowired
    public JwtAccessTokenAdapter(JwtService jwtService, AppSecurityProps props) {
        this.jwtService = jwtService;
        this.props = props;
    }

    @Override
    public String createAccessToken(String subjectUserId, List<String> roles, Map<String, Object> extraClaims) {
        return jwtService.createAccessToken(subjectUserId, roles, extraClaims);
    }

    @Override
    public long getAccessTokenTtlSeconds() {
        return props.getJwt().getAccessTokenTtl().toSeconds();
    }
}
