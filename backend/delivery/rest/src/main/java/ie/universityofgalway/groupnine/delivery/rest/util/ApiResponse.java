package ie.universityofgalway.groupnine.delivery.rest.util;

import java.time.Instant;

/**
 * Unified API response wrapper.
 * Every endpoint (success or error) should return this envelope.
 */
public class ApiResponse<T> {
    private final Instant timestamp = Instant.now();
    private final boolean success;
    private final T data;
    private final ApiError error;

    private ApiResponse(boolean success, T data, ApiError error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return new ApiResponse<>(false, null, error);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }
}