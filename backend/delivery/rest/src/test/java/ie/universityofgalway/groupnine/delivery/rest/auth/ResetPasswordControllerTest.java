package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.service.auth.usecase.ResetPasswordUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResetPasswordControllerTest {
    MockMvc mockMvc;
    ResetPasswordUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = Mockito.mock(ResetPasswordUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ResetPasswordController(useCase)).build();
    }

    @Test
    void returns_no_content_on_success() throws Exception {
        String json = "{\n  \"token\": \"opaque\",\n  \"password\": \"new-password-123\"\n}";
        mockMvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));
        Mockito.verify(useCase).execute(eq("opaque"), eq("new-password-123"));
    }
}
