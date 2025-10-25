package ie.universityofgalway.groupnine.delivery.rest.support;

import ie.universityofgalway.groupnine.delivery.rest.util.ApiError;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiErrorTest {

    @Test
    void createsApiErrorWithAllFieldsSetCorrectly() {
        int status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/api/resource";
        List<String> details = List.of("detail1", "detail2");

        ApiError apiError = new ApiError(status, error, message, path, details);

        assertEquals(status, apiError.getStatus());
        assertEquals(error, apiError.getError());
        assertEquals(message, apiError.getMessage());
        assertEquals(path, apiError.getPath());
        assertEquals(details, apiError.getDetails());
        assertNotNull(apiError.getTimestamp());
    }

    @Test
    void timestampIsSetToCurrentInstantOnCreation() {
        ApiError apiError = new ApiError(500, "Internal Error", "Something went wrong", "/api/error", Collections.emptyList());
        Instant now = Instant.now();
        assertTrue(!apiError.getTimestamp().isAfter(now));
    }

    @Test
    void detailsCanBeEmptyList() {
        ApiError apiError = new ApiError(400, "Bad Request", "Invalid input", "/api/input", Collections.emptyList());
        assertTrue(apiError.getDetails().isEmpty());
    }

    @Test
    void allowsNullValuesForOptionalFields() {
        ApiError apiError = new ApiError(401, null, null, null, null);
        assertEquals(401, apiError.getStatus());
        assertNull(apiError.getError());
        assertNull(apiError.getMessage());
        assertNull(apiError.getPath());
        assertNull(apiError.getDetails());
    }
}
