package ie.universityofgalway.groupnine.delivery.rest.util;

import ie.universityofgalway.groupnine.util.logging.AppLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Maps common exceptions to a consistent JSON error body.
 * Keeps controllers thin and free of try/catch noise.
 */
@RestControllerAdvice(basePackages = "ie.universityofgalway.groupnine.delivery.rest")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final AppLogger log = AppLogger.get(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Invalid parameter",
                request.getDescription(false).replace("uri=", ""),
                List.of(ex.getName() + " must be of type " +
                        (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "expected"))
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        log.info("bad_request_illegal_argument", "message", ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                List.of()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        log.error("internal_error", "exception", ex.getClass().getSimpleName(), "message", ex.getMessage());
        ApiError body = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected error",
                request.getDescription(false).replace("uri=", ""),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex, WebRequest request) {
        ApiError body = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage() == null ? "Not found" : ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
