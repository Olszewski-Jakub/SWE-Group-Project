package ie.universityofgalway.groupnine.delivery.rest.support;

import ie.universityofgalway.groupnine.delivery.rest.util.ApiError;
import ie.universityofgalway.groupnine.delivery.rest.util.ApiResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    @Test
    void okSetsSuccessTrueAndDataNonNullAndErrorNull() {
        String payload = "result";
        ApiResponse<String> response = ApiResponse.ok(payload);

        assertTrue(response.isSuccess());
        assertEquals(payload, response.getData());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void failSetsSuccessFalseAndErrorNonNullAndDataNull() {
        ApiError error = new ApiError(400, "Bad Request", "Invalid", "/api", null);
        ApiResponse<Object> response = ApiResponse.fail(error);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(error, response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void timestampIsSetToCurrentInstantOnCreation() {
        ApiResponse<String> response = ApiResponse.ok("data");
        Instant now = Instant.now();
        assertTrue(!response.getTimestamp().isAfter(now));
    }

    @Test
    void okAllowsNullData() {
        ApiResponse<Object> response = ApiResponse.ok(null);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertNull(response.getError());
    }

    @Test
    void failAllowsNullError() {
        ApiResponse<Object> response = ApiResponse.fail(null);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNull(response.getError());
    }
}
