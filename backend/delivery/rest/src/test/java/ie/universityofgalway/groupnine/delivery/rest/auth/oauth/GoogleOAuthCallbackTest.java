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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = GoogleOAuthController.class)
@Import(ie.universityofgalway.groupnine.delivery.rest.rbac.RbacTestSecurityConfig.class)
class GoogleOAuthCallbackTest extends CommonWebMvcTest {

    @MockitoBean private GoogleOAuthProps googleProps;
    @MockitoBean private GoogleOAuthFlowUseCase flow;
    @MockitoBean private ie.universityofgalway.groupnine.security.web.TokenCookieFactory cookieFactory;

    @Test
    void callback_exchanges_code_and_sets_cookie() throws Exception {
        when(cookieFactory.buildRefreshCookie("refr")).thenReturn(org.springframework.http.ResponseCookie.from("refreshToken","refr").path("/").build());
        when(cookieFactory.buildAccessCookie("acc")).thenReturn(org.springframework.http.ResponseCookie.from("accessToken","acc").path("/").build());
        when(flow.handleCallback(eq("cd"), eq("st"), any(), any())).thenReturn(new GoogleOAuthFlowUseCase.CallbackResult("acc", "refr", 900L, null));

        mockMvc.perform(get("/api/v1/auth/oauth/google/callback")
                        .param("code", "cd")
                        .param("state", "st")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=refr")))
                .andExpect(content().string(containsString("\"accessToken\":\"acc\"")));
    }
}
