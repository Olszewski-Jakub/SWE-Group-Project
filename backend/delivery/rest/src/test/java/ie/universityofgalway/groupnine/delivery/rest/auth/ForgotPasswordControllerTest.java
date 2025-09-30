package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.service.auth.usecase.RequestPasswordResetUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ForgotPasswordControllerTest {
    MockMvc mockMvc;
    RequestPasswordResetUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = Mockito.mock(RequestPasswordResetUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ForgotPasswordController(useCase)).build();
    }

    @Test
    void accepts_request_and_returns_202() throws Exception {
        String json = "{\n  \"email\": \"user@example.com\",\n  \"locale\": \"en\"\n}";
        mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(json)
                        .header("X-Forwarded-For", "203.0.113.1"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").exists());
        Mockito.verify(useCase).execute(eq("user@example.com"), eq("en"), any());
    }
}
