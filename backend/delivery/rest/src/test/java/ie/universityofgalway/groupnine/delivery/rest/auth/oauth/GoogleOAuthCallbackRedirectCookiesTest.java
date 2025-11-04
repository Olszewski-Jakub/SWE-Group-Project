package ie.universityofgalway.groupnine.delivery.rest.auth.oauth;

import ie.universityofgalway.groupnine.security.config.props.GoogleOAuthProps;
import ie.universityofgalway.groupnine.service.auth.usecase.GoogleOAuthFlowUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = GoogleOAuthController.class)
@Import(ie.universityofgalway.groupnine.delivery.rest.rbac.RbacTestSecurityConfig.class)
class GoogleOAuthCallbackRedirectCookiesTest extends CommonWebMvcTest {

    @MockitoBean private GoogleOAuthProps googleProps;
    @MockitoBean private GoogleOAuthFlowUseCase flow;
    @MockitoBean private ie.universityofgalway.groupnine.security.web.TokenCookieFactory cookieFactory;

    @Test
    void callback_sets_both_cookies_and_302_to_frontend() throws Exception {
        // Configure allowed redirect
        when(googleProps.getAllowedRedirects()).thenReturn(java.util.List.of("https://app.example.com"));

        when(cookieFactory.buildRefreshCookie("refr")).thenReturn(org.springframework.http.ResponseCookie.from("refreshToken","refr").path("/").build());
        when(cookieFactory.buildAccessCookie("acc")).thenReturn(org.springframework.http.ResponseCookie.from("accessToken","acc").path("/").build());

        // Flow result
        when(flow.handleCallback(eq("cd"), eq("st"), any(), any())).thenReturn(new GoogleOAuthFlowUseCase.CallbackResult("acc", "refr", 900L, "https://app.example.com/welcome"));

        mockMvc.perform(get("/api/v1/auth/oauth/google/callback").param("code", "cd").param("state", "st"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/html")))
                .andExpect(result -> {
                    java.util.List<String> cookies = result.getResponse().getHeaders("Set-Cookie");
                    org.junit.jupiter.api.Assertions.assertTrue(cookies.stream().anyMatch(h -> h.contains("refreshToken=refr")), "refresh cookie missing");
                    org.junit.jupiter.api.Assertions.assertTrue(cookies.stream().anyMatch(h -> h.contains("accessToken=acc")), "access cookie missing");
                });
    }
}
