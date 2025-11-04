package ie.universityofgalway.groupnine.integration.oauth;

import ie.universityofgalway.groupnine.security.config.props.GoogleOAuthProps;
import ie.universityofgalway.groupnine.service.auth.port.GoogleOAuthClientPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleOAuthClientAdapter implements GoogleOAuthClientPort {
    private final GoogleOAuthProps props;
    private final RestTemplate rest = new RestTemplate();

    public GoogleOAuthClientAdapter(GoogleOAuthProps props) {
        this.props = props;
    }

    @Override
    public TokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("redirect_uri", props.getRedirectUri());
        form.add("grant_type", "authorization_code");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<GoogleTokenResponse> res = rest.postForEntity(props.getTokenUri(), new HttpEntity<>(form, headers), GoogleTokenResponse.class);
        GoogleTokenResponse body = res.getBody();
        if (body == null) return null;
        TokenResponse out = new TokenResponse();
        out.accessToken = body.access_token;
        out.idToken = body.id_token;
        out.refreshToken = body.refresh_token;
        out.tokenType = body.token_type;
        out.expiresIn = body.expires_in;
        return out;
    }

    @Override
    public UserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        HttpEntity<Void> http = new HttpEntity<>(headers);
        ResponseEntity<GoogleUserInfo> res = rest.exchange(props.getUserInfoUri() + "?alt=json", org.springframework.http.HttpMethod.GET, http, GoogleUserInfo.class);
        GoogleUserInfo in = res.getBody();
        if (in == null) return null;
        UserInfo out = new UserInfo();
        out.sub = in.sub;
        out.email = in.email;
        out.emailVerified = in.email_verified;
        out.givenName = in.given_name;
        out.familyName = in.family_name;
        out.name = in.name;
        out.picture = in.picture;
        return out;
    }

    static final class GoogleTokenResponse { public String access_token, id_token, refresh_token, token_type; public Long expires_in; }
    static final class GoogleUserInfo { public String sub,email,name,picture,given_name,family_name; public Boolean email_verified; }
}

