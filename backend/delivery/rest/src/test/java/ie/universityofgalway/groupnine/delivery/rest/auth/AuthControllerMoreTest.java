package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.security.config.props.AuthProps;
import ie.universityofgalway.groupnine.service.auth.usecase.LoginUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutAllUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RefreshUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RegisterUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.VerifyEmailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerMoreTest {
    private MockMvc mockMvc;
    private RefreshUseCase refreshUseCase;
    private LogoutUseCase logoutUseCase;
    private LogoutAllUseCase logoutAllUseCase;

    @BeforeEach
    void setup() {
        refreshUseCase = Mockito.mock(RefreshUseCase.class);
        logoutUseCase = Mockito.mock(LogoutUseCase.class);
        logoutAllUseCase = Mockito.mock(LogoutAllUseCase.class);
        AuthProps props = new AuthProps();
        props.setRefreshCookieName("refreshToken");
        props.setRefreshTtlDays(14);

        AuthController controller = new AuthController(
                Mockito.mock(RegisterUserUseCase.class),
                Mockito.mock(VerifyEmailUseCase.class),
                Mockito.mock(LoginUseCase.class),
                refreshUseCase,
                logoutUseCase,
                logoutAllUseCase,
                props
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AuthExceptionHandler())
                .build();
    }

    @Test
    void refreshUsesHeaderAndSetsCookie() throws Exception {
        when(refreshUseCase.execute(eq("old"), any(), any())).thenReturn(new RefreshUseCase.Result("acc", 900, "new"));
        mockMvc.perform(post("/api/v1/auth/refresh").header("X-Refresh-Token", "old"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"refreshToken\":\"new\"")))
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=new")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")));
    }

    @Test
    void refreshUsesCookieWhenHeaderMissing() throws Exception {
        when(refreshUseCase.execute(eq("cookieTok"), any(), any())).thenReturn(new RefreshUseCase.Result("acc", 900, "next"));
        MockHttpServletRequestBuilder req = post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", "cookieTok"));
        mockMvc.perform(req)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"refreshToken\":\"next\"")))
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=next")));
    }

    @Test
    void logoutClearsCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout").header("X-Refresh-Token", "tok"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }

    @Test
    void logoutAllClearsCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout-all").header("X-Refresh-Token", "tok"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }
}
