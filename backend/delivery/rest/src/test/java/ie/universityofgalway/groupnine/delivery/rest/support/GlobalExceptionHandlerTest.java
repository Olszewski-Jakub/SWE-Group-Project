package ie.universityofgalway.groupnine.delivery.rest.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler")
@Nested
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void returnsBadRequestWithTypeMismatchDetails() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        WebRequest request = mock(WebRequest.class);

        when(ex.getName()).thenReturn("id");
        when(request.getDescription(false)).thenReturn("uri=/api/resource");

        ResponseEntity<ApiError> response = handler.handleTypeMismatch(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid parameter", response.getBody().getMessage());
        assertEquals("/api/resource", response.getBody().getPath());
        assertFalse(response.getBody().getDetails().get(0).contains("id must be of type Integer"));
    }

    @Test
    void returnsBadRequestWithTypeMismatchDetailsWhenRequiredTypeIsNull() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        WebRequest request = mock(WebRequest.class);

        when(ex.getName()).thenReturn("param");
        when(ex.getRequiredType()).thenReturn(null);
        when(request.getDescription(false)).thenReturn("uri=/api/test");

        ResponseEntity<ApiError> response = handler.handleTypeMismatch(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getDetails().get(0).contains("param must be of type expected"));
    }

    @Test
    void returnsBadRequestWithIllegalArgumentMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid value");
        WebRequest request = mock(WebRequest.class);

        when(request.getDescription(false)).thenReturn("uri=/api/illegal");

        ResponseEntity<ApiError> response = handler.handleIllegalArgument(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid value", response.getBody().getMessage());
        assertEquals("/api/illegal", response.getBody().getPath());
        assertTrue(response.getBody().getDetails().isEmpty());
    }

    @Test
    void returnsInternalServerErrorForGenericException() {
        Exception ex = new Exception("Something failed");
        WebRequest request = mock(WebRequest.class);

        when(request.getDescription(false)).thenReturn("uri=/api/generic");

        ResponseEntity<ApiError> response = handler.handleGeneric(ex, request);

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error", response.getBody().getMessage());
        assertEquals("/api/generic", response.getBody().getPath());
        assertTrue(response.getBody().getDetails().isEmpty());
    }
}
