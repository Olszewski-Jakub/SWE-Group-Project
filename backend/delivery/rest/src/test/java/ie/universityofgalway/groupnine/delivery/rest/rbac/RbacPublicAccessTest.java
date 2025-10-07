package ie.universityofgalway.groupnine.delivery.rest.rbac;

import ie.universityofgalway.groupnine.delivery.rest.auth.AuthController;
import ie.universityofgalway.groupnine.delivery.rest.health.HealthController;
import ie.universityofgalway.groupnine.domain.health.HealthStatus;
import ie.universityofgalway.groupnine.service.auth.usecase.LoginUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutAllUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RefreshUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RegisterUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.VerifyEmailUseCase;
import ie.universityofgalway.groupnine.service.health.HealthCheckUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Public access tests ensuring endpoints annotated with @PublicEndpoint are reachable
 * without authentication (e.g., health and authentication endpoints).
 */
@DeliveryWebMvcTest(controllers = {HealthController.class, AuthController.class})
@Import(RbacTestSecurityConfig.class)
class RbacPublicAccessTest extends CommonWebMvcTest {

    @MockitoBean
    private HealthCheckUseCase healthUseCase;

    // Auth controller dependencies (not invoked here, just to satisfy wiring)
    @MockitoBean private RegisterUserUseCase registerUserUseCase;
    @MockitoBean private VerifyEmailUseCase verifyEmailUseCase;
    @MockitoBean private LoginUseCase loginUseCase;
    @MockitoBean private RefreshUseCase refreshUseCase;
    @MockitoBean private LogoutUseCase logoutUseCase;
    @MockitoBean private LogoutAllUseCase logoutAllUseCase;
    @MockitoBean private ie.universityofgalway.groupnine.security.config.props.AuthProps authProps;

    @Test
    void health_is_public() throws Exception {
        when(healthUseCase.checkHealth()).thenReturn(HealthStatus.UP);
        mockMvc.perform(get("/api/v1/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void auth_login_is_public() throws Exception {
        // No body provided → should not be 401/403 due to RBAC; expect 400 Bad Request from MVC validation
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
    }
}
