package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.util.ApiError;
import ie.universityofgalway.groupnine.domain.auth.TooManyAttempts;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthExceptionHandlerTest {
    @Test
    void mapsTooManyAttemptsTo429WithRetryAfter() {
        AuthExceptionHandler handler = new AuthExceptionHandler();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/v1/auth/login");
        TooManyAttempts ex = new TooManyAttempts("Too many attempts. Retry after 120 seconds.", 120);
        ResponseEntity<ApiError> resp = handler.handleTooMany(ex, req);
        assertEquals(429, resp.getStatusCode().value());
        assertTrue(resp.getHeaders().containsKey("Retry-After"));
        assertEquals("120", resp.getHeaders().getFirst("Retry-After"));
        assertTrue(resp.getBody().getMessage().contains("Retry after 120"));
    }
}
