package ie.universityofgalway.groupnine.delivery.rest.health;

import ie.universityofgalway.groupnine.domain.health.HealthStatus;
import ie.universityofgalway.groupnine.service.auth.usecase.RegisterUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.VerifyEmailUseCase;
import ie.universityofgalway.groupnine.service.health.HealthCheckUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = HealthController.class)
class HealthControllerTest extends CommonWebMvcTest {

    @MockitoBean
    private HealthCheckUseCase useCase;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;
    @MockitoBean
    private VerifyEmailUseCase verifyEmailUseCase;

    @Test
    void returns200_whenHealthUp() throws Exception {
        when(useCase.checkHealth()).thenReturn(HealthStatus.UP);

        mockMvc.perform(get("/api/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void returns503_whenHealthDown() throws Exception {
        when(useCase.checkHealth()).thenReturn(HealthStatus.DOWN);

        mockMvc.perform(get("/api/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("DOWN"));
    }
}
