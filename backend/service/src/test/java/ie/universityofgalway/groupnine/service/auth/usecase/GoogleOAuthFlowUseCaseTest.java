package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.service.auth.port.GoogleOAuthClientPort;
import ie.universityofgalway.groupnine.service.auth.port.GoogleOAuthConfigPort;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.service.auth.port.OAuthAccountRepositoryPort;
import ie.universityofgalway.groupnine.service.auth.port.OAuthStatePort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleOAuthFlowUseCaseTest {
    @Test
    void buildAuthorizationUrl_contains_core_params() {
        GoogleOAuthConfigPort cfg = mock(GoogleOAuthConfigPort.class);
        when(cfg.getAuthorizationUri()).thenReturn("https://accounts.google.com/o/oauth2/v2/auth");
        when(cfg.getClientId()).thenReturn("cid");
        when(cfg.getRedirectUri()).thenReturn("https://api.example.com/callback");
        when(cfg.getScopes()).thenReturn(List.of("openid","email"));
        when(cfg.getAllowedRedirects()).thenReturn(List.of("https://app.example.com"));
        OAuthStatePort state = mock(OAuthStatePort.class);
        when(state.create(any())).thenReturn("st");
        GoogleOAuthClientPort google = mock(GoogleOAuthClientPort.class);
        SocialLoginUseCase social = mock(SocialLoginUseCase.class);
        UserRepositoryPort users = mock(UserRepositoryPort.class);
        OAuthAccountRepositoryPort oauth = mock(OAuthAccountRepositoryPort.class);

        GoogleOAuthFlowUseCase uc = new GoogleOAuthFlowUseCase(cfg, state, google, social, users, oauth);
        String url = uc.buildAuthorizationUrl("https://app.example.com");
        assertTrue(url.contains("client_id=cid"));
        assertTrue(url.contains("response_type=code"));
        assertTrue(url.contains("state=st"));
    }

    @Test
    void handleCallback_happy_path_returns_tokens() throws Exception {
        GoogleOAuthConfigPort cfg = mock(GoogleOAuthConfigPort.class);
        when(cfg.getAllowedRedirects()).thenReturn(List.of("https://app.example.com"));
        OAuthStatePort state = mock(OAuthStatePort.class);
        when(state.verify("st")).thenReturn(new OAuthStatePort.Parsed("n","https://app.example.com/home"));
        GoogleOAuthClientPort google = mock(GoogleOAuthClientPort.class);
        GoogleOAuthClientPort.TokenResponse tr = new GoogleOAuthClientPort.TokenResponse();
        tr.accessToken = "ga";
        when(google.exchangeCode("cd")).thenReturn(tr);
        GoogleOAuthClientPort.UserInfo ui = new GoogleOAuthClientPort.UserInfo();
        ui.email = "jane@example.com";
        ui.givenName = "Jane";
        ui.familyName = "Doe";
        ui.emailVerified = true;
        when(google.getUserInfo("ga")).thenReturn(ui);
        SocialLoginUseCase social = mock(SocialLoginUseCase.class);
        when(social.execute(eq("jane@example.com"), eq("Jane"), eq("Doe"), eq(true), any(), any()))
                .thenReturn(new SocialLoginUseCase.Result("acc", 900L, "refr"));

        UserRepositoryPort users = mock(UserRepositoryPort.class);
        when(users.findByEmail(Email.of("jane@example.com"))).thenReturn(java.util.Optional.of(
                new User(UserId.newId(), Email.of("jane@example.com"), "Jane","Doe", UserStatus.ACTIVE, true, null, java.time.Instant.now(), java.time.Instant.now(), java.util.Set.of())
        ));
        OAuthAccountRepositoryPort oauth = mock(OAuthAccountRepositoryPort.class);

        GoogleOAuthFlowUseCase uc = new GoogleOAuthFlowUseCase(cfg, state, google, social, users, oauth);
        GoogleOAuthFlowUseCase.CallbackResult rs = uc.handleCallback("cd", "st", "ua", InetAddress.getByName("127.0.0.1"));
        assertEquals("acc", rs.accessToken());
        assertEquals("refr", rs.refreshToken());
        assertEquals("https://app.example.com/home", rs.redirectTarget());
    }
}
