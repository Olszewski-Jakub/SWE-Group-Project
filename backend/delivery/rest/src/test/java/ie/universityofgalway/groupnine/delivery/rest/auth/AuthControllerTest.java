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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {
    private MockMvc mockMvc;
    private LoginUseCase loginUseCase;
    private RefreshUseCase refreshUseCase;
    private LogoutUseCase logoutUseCase;
    private LogoutAllUseCase logoutAllUseCase;
    private AuthProps props;

    @BeforeEach
    void setup() {
        loginUseCase = Mockito.mock(LoginUseCase.class);
        refreshUseCase = Mockito.mock(RefreshUseCase.class);
        logoutUseCase = Mockito.mock(LogoutUseCase.class);
        logoutAllUseCase = Mockito.mock(LogoutAllUseCase.class);
        props = new AuthProps();
        props.setRefreshCookieName("refreshToken");
        props.setRefreshTtlDays(14);

        AuthController controller = new AuthController(
                Mockito.mock(RegisterUserUseCase.class),
                Mockito.mock(VerifyEmailUseCase.class),
                loginUseCase,
                refreshUseCase,
                logoutUseCase,
                logoutAllUseCase,
                props
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginSetsCookieAndReturnsRefreshToken() throws Exception {
        when(loginUseCase.execute(eq("user@example.com"), eq("pass"), any(), any()))
                .thenReturn(new LoginUseCase.Result("acc", 900, "opaque_refresh"));

        String json = "{\n  \"email\": \"user@example.com\",\n  \"password\": \"pass\"\n}";
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=opaque_refresh")))
                .andExpect(content().string(containsString("\"refreshToken\":\"opaque_refresh\"")));
    }
}
