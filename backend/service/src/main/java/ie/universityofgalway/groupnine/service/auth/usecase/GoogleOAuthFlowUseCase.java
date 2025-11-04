package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.auth.OAuthProvider;
import ie.universityofgalway.groupnine.service.auth.port.GoogleOAuthClientPort;
import ie.universityofgalway.groupnine.service.auth.port.GoogleOAuthConfigPort;
import ie.universityofgalway.groupnine.service.auth.port.OAuthAccountRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.OAuthStatePort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import ie.universityofgalway.groupnine.util.url.UrlUtility;

import java.net.InetAddress;
import java.util.List;

public class GoogleOAuthFlowUseCase {
    private final GoogleOAuthConfigPort cfg;
    private final OAuthStatePort state;
    private final GoogleOAuthClientPort google;
    private final SocialLoginUseCase socialLogin;
    private final UserRepositoryPort users;
    private final OAuthAccountRepositoryPort oauthAccounts;

    public GoogleOAuthFlowUseCase(GoogleOAuthConfigPort cfg,
                                  OAuthStatePort state,
                                  GoogleOAuthClientPort google,
                                  SocialLoginUseCase socialLogin,
                                  UserRepositoryPort users,
                                  OAuthAccountRepositoryPort oauthAccounts) {
        this.cfg = cfg;
        this.state = state;
        this.google = google;
        this.socialLogin = socialLogin;
        this.users = users;
        this.oauthAccounts = oauthAccounts;
    }

    public String buildAuthorizationUrl(String requestedRedirect) {
        String safeRedirect = isAllowedRedirect(requestedRedirect) ? requestedRedirect : null;
        String stateToken = state.create(safeRedirect);

        java.net.URI base = UrlUtility.toURI(cfg.getAuthorizationUri());
        UrlUtility.Builder builder = new UrlUtility.Builder()
                .scheme(base.getScheme())
                .host(base.getHost());
        if (base.getPort() != -1) {
            builder.port(base.getPort());
        }
        String path = base.getPath();
        if (path != null && !path.isBlank()) {
            for (String seg : path.split("/")) {
                if (!seg.isEmpty()) builder.addPathSegment(seg);
            }
        }
        builder
                .queryParam("client_id", cfg.getClientId())
                .queryParam("redirect_uri", cfg.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", defaultScopes()))
                .queryParam("access_type", "offline")
                .queryParam("include_granted_scopes", "true")
                .queryParam("prompt", "consent")
                .queryParam("state", stateToken);
        return builder.build();
    }

    public CallbackResult handleCallback(String code, String stateToken, String userAgent, InetAddress ip) {
        if (stateToken == null || stateToken.isBlank() || code == null || code.isBlank()) {
            throw new IllegalArgumentException("missing_code_or_state");
        }
        OAuthStatePort.Parsed parsed = state.verify(stateToken);
        GoogleOAuthClientPort.TokenResponse tok = google.exchangeCode(code);
        if (tok == null || tok.accessToken == null || tok.accessToken.isBlank()) {
            throw new IllegalStateException("token_exchange_failed");
        }
        GoogleOAuthClientPort.UserInfo info = google.getUserInfo(tok.accessToken);
        if (info == null || info.email == null || info.email.isBlank()) {
            throw new IllegalStateException("user_info_failed");
        }
        boolean emailVerified = Boolean.TRUE.equals(info.emailVerified);
        SocialLoginUseCase.Result login = socialLogin.execute(info.email, info.givenName, info.familyName, emailVerified, userAgent, ip);

        User user = users.findByEmail(Email.of(info.email)).orElse(null);
        if (user != null && info.sub != null && !info.sub.isBlank()) {
            OAuthProvider provider = OAuthProvider.GOOGLE;
            if (!oauthAccounts.isLinked(provider, user.getId().value())
                    && !oauthAccounts.existsByProviderUserId(provider, info.sub)) {
                oauthAccounts.link(provider, info.sub, info.email, user.getId().value());
            }
        }
        String redirect = isAllowedRedirect(parsed.redirect()) ? parsed.redirect() : null;
        return new CallbackResult(login.accessToken(), login.refreshToken(), login.expiresInSeconds(), redirect);
    }

    private List<String> defaultScopes() {
        List<String> s = cfg.getScopes();
        return (s == null || s.isEmpty()) ? List.of("openid", "email", "profile") : s;
    }

    private boolean isAllowedRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) return false;
        List<String> allowed = cfg.getAllowedRedirects();
        if (allowed == null || allowed.isEmpty()) return false;
        return allowed.stream().anyMatch(redirect::startsWith);
    }

    public record CallbackResult(String accessToken, String refreshToken, long expiresInSeconds, String redirectTarget) {}
}
