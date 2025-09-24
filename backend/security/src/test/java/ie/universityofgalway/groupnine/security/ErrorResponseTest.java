package ie.universityofgalway.groupnine.security;

import ie.universityofgalway.groupnine.security.web.ErrorResponse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void returnsErrorAndMessageWhenBothProvided() {
        ErrorResponse response = new ErrorResponse("NOT_FOUND", "Resource not found");
        assertThat(response.error()).isEqualTo("NOT_FOUND");
        assertThat(response.message()).isEqualTo("Resource not found");
    }

    @Test
    void returnsErrorAndNullMessageWhenMessageIsNull() {
        ErrorResponse response = new ErrorResponse("BAD_REQUEST", null);
        assertThat(response.error()).isEqualTo("BAD_REQUEST");
        assertThat(response.message()).isNull();
    }

    @Test
    void returnsNullErrorAndMessageWhenBothAreNull() {
        ErrorResponse response = new ErrorResponse(null, null);
        assertThat(response.error()).isNull();
        assertThat(response.message()).isNull();
    }

    @Test
    void returnsErrorAndEmptyMessageWhenMessageIsEmptyString() {
        ErrorResponse response = new ErrorResponse("FORBIDDEN", "");
        assertThat(response.error()).isEqualTo("FORBIDDEN");
        assertThat(response.message()).isEmpty();
    }

    @Test
    void returnsEmptyErrorAndMessageWhenErrorIsEmptyString() {
        ErrorResponse response = new ErrorResponse("", "Some message");
        assertThat(response.error()).isEmpty();
        assertThat(response.message()).isEqualTo("Some message");
    }
}
