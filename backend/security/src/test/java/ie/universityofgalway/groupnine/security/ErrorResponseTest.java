package ie.universityofgalway.groupnine.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void returnsErrorAndMessageWhenBothProvided() {
        ErrorResponse response = new ErrorResponse("NOT_FOUND", "Resource not found");
        assertThat(response.getError()).isEqualTo("NOT_FOUND");
        assertThat(response.getMessage()).isEqualTo("Resource not found");
    }

    @Test
    void returnsErrorAndNullMessageWhenMessageIsNull() {
        ErrorResponse response = new ErrorResponse("BAD_REQUEST", null);
        assertThat(response.getError()).isEqualTo("BAD_REQUEST");
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void returnsNullErrorAndMessageWhenBothAreNull() {
        ErrorResponse response = new ErrorResponse(null, null);
        assertThat(response.getError()).isNull();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void returnsErrorAndEmptyMessageWhenMessageIsEmptyString() {
        ErrorResponse response = new ErrorResponse("FORBIDDEN", "");
        assertThat(response.getError()).isEqualTo("FORBIDDEN");
        assertThat(response.getMessage()).isEmpty();
    }

    @Test
    void returnsEmptyErrorAndMessageWhenErrorIsEmptyString() {
        ErrorResponse response = new ErrorResponse("", "Some message");
        assertThat(response.getError()).isEmpty();
        assertThat(response.getMessage()).isEqualTo("Some message");
    }
}
