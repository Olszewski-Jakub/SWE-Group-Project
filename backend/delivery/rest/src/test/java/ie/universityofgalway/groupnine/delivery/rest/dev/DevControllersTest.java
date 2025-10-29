package ie.universityofgalway.groupnine.delivery.rest.dev;

import ie.universityofgalway.groupnine.security.jwt.JwtService;
import ie.universityofgalway.groupnine.service.auth.port.BruteForceGuardPort;
import ie.universityofgalway.groupnine.service.session.usecase.GetSessionChainUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = {DevTokenController.class, DevBruteForceController.class, DevSessionController.class})
class DevControllersTest extends CommonWebMvcTest {

    @MockitoBean JwtService jwtService;
    @MockitoBean
    BruteForceGuardPort bruteForceGuardPort;
    @MockitoBean GetSessionChainUseCase getSessionChainUseCase;

    @Test
    void devToken_returnsToken() throws Exception {
        given(jwtService.createAccessToken(any(), any(), any())).willReturn("tok");
        mockMvc.perform(get("/dev/token").param("roles","ADMIN").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok"));
    }

    @Test
    void bruteForce_which_returnsImplementation() throws Exception {
        mockMvc.perform(get("/dev/bruteforce/which").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.implementation").exists());
    }

    @Test
    void session_chain_requiresExactlyOneParam() throws Exception {
        mockMvc.perform(get("/dev/sessions/chain").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/dev/sessions/chain").param("sessionId", UUID.randomUUID().toString()).param("refreshToken","x"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void session_chain_bySessionId_returnsNodes() throws Exception {
        given(getSessionChainUseCase.bySessionId(any())).willReturn(List.of());
        mockMvc.perform(get("/dev/sessions/chain").param("sessionId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes").exists());
    }
}

