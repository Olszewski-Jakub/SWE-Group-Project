package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.support.ApiError;
import ie.universityofgalway.groupnine.domain.auth.TooManyAttempts;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthExceptionHandlerTest {
    @Test
    void mapsTooManyAttemptsTo429WithRetryAfter() {
        AuthExceptionHandler handler = new AuthExceptionHandler();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/auth/login");
        TooManyAttempts ex = new TooManyAttempts("Too many attempts. Retry after 120 seconds.", 120);
        ResponseEntity<ApiError> resp = handler.handleTooMany(ex, req);
        assertEquals(429, resp.getStatusCode().value());
        assertTrue(resp.getHeaders().containsKey("Retry-After"));
        assertEquals("120", resp.getHeaders().getFirst("Retry-After"));
        assertTrue(resp.getBody().getMessage().contains("Retry after 120"));
    }
}

