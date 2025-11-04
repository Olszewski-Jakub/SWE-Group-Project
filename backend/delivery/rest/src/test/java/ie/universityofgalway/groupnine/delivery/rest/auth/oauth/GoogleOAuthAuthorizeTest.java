// java
package ie.universityofgalway.groupnine.delivery.rest.auth.oauth;

import ie.universityofgalway.groupnine.security.config.props.GoogleOAuthProps;
import ie.universityofgalway.groupnine.service.auth.usecase.GoogleOAuthFlowUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = GoogleOAuthController.class)
@Import(ie.universityofgalway.groupnine.delivery.rest.rbac.RbacTestSecurityConfig.class)
class GoogleOAuthAuthorizeTest extends CommonWebMvcTest {

    @MockitoBean private GoogleOAuthProps props;
    @MockitoBean private GoogleOAuthFlowUseCase flow;
    @MockitoBean private ie.universityofgalway.groupnine.security.web.TokenCookieFactory cookieFactory;

    @Test
    void authorize_is_public_and_returns_url() throws Exception {
        when(flow.buildAuthorizationUrl(any())).thenReturn(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=client-id&response_type=code&scope=openid&state=opaque-state"
        );

        mockMvc.perform(get("/api/v1/auth/oauth/google/authorize").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorizationUrl", containsString("accounts.google.com")))
                .andExpect(jsonPath("$.authorizationUrl", containsString("client_id=client-id")))
                .andExpect(jsonPath("$.authorizationUrl", containsString("response_type=code")))
                .andExpect(jsonPath("$.authorizationUrl", containsString("scope=openid")))
                .andExpect(jsonPath("$.authorizationUrl", containsString("state=opaque-state")));
    }
}