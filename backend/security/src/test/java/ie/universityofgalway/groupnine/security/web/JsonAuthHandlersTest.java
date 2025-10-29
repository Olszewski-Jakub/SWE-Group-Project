package ie.universityofgalway.groupnine.security.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class JsonAuthHandlersTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonAuthHandlers handlers = new JsonAuthHandlers(mapper);

    @Test
    void authenticationEntryPointReturnsJson401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        handlers.authenticationEntryPoint().commence(req, res,
                new AuthenticationCredentialsNotFoundException("Authentication required"));

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentType()).isEqualTo("application/json");
        JsonNode json = mapper.readTree(res.getContentAsByteArray());
        assertThat(json.get("error").asText()).isEqualTo("unauthorized");
        assertThat(json.get("message").asText()).contains("Authentication required");
    }

    @Test
    void accessDeniedHandlerReturnsJson403() throws Exception {
        HttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        handlers.accessDeniedHandler().handle(req, res, new AccessDeniedException("Forbidden"));

        assertThat(res.getStatus()).isEqualTo(403);
        assertThat(res.getContentType()).isEqualTo("application/json");
        JsonNode json = mapper.readTree(res.getContentAsByteArray());
        assertThat(json.get("error").asText()).isEqualTo("forbidden");
        assertThat(json.get("message").asText()).contains("Forbidden");
    }
}

