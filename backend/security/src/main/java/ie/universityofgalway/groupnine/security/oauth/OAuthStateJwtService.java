package ie.universityofgalway.groupnine.security.oauth;

import ie.universityofgalway.groupnine.security.jwt.JwtService;
import ie.universityofgalway.groupnine.service.auth.port.OAuthStatePort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class OAuthStateJwtService implements OAuthStatePort {
    private final JwtService jwt;

    public OAuthStateJwtService(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    public String create(String redirect) {
        String nonce = UUID.randomUUID().toString();
        Map<String, Object> extra = Map.of(
                "typ", "oauth_state",
                "nonce", nonce,
                "redirect", redirect == null ? "" : redirect
        );
        return jwt.createAccessToken(nonce, java.util.List.of(), extra);
    }

    @Override
    public OAuthStatePort.Parsed verify(String stateToken) {
        ie.universityofgalway.groupnine.security.jwt.JwtClaims claims = jwt.validate(stateToken);
        Object typ = claims.getClaims().get("typ");
        if (!"oauth_state".equals(String.valueOf(typ))) {
            throw new IllegalArgumentException("Invalid state type");
        }
        String redirect = String.valueOf(claims.getClaims().getOrDefault("redirect", ""));
        String nonce = String.valueOf(claims.getClaims().getOrDefault("nonce", ""));
        return new OAuthStatePort.Parsed(nonce, redirect == null || redirect.isBlank() ? null : redirect);
    }
}

