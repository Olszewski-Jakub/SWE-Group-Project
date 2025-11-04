package ie.universityofgalway.groupnine.delivery.rest.auth.oauth;

import ie.universityofgalway.groupnine.service.auth.usecase.GoogleOAuthFlowUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import ie.universityofgalway.groupnine.security.config.props.GoogleOAuthProps;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = GoogleOAuthController.class)
@Import(ie.universityofgalway.groupnine.delivery.rest.rbac.RbacTestSecurityConfig.class)
class GoogleOAuthAuthorizeRedirectTest extends CommonWebMvcTest {
    @MockitoBean private GoogleOAuthProps props;
    @MockitoBean private GoogleOAuthFlowUseCase flow;
    @MockitoBean private ie.universityofgalway.groupnine.security.web.TokenCookieFactory cookieFactory;

    @Test
    void authorize_redirect_302_to_google() throws Exception {
        when(flow.buildAuthorizationUrl(any())).thenReturn("https://accounts.google.com/o/oauth2/v2/auth?client_id=client-id&state=st");

        mockMvc.perform(get("/api/v1/auth/oauth/google/authorize/redirect").param("redirect", "https://app.example.com/welcome"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("accounts.google.com")))
                .andExpect(header().string("Location", containsString("client_id=client-id")))
                .andExpect(header().string("Location", containsString("state=st")));
    }
}
