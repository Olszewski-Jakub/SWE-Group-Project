package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.support.ApiError;
import ie.universityofgalway.groupnine.domain.auth.EmailAlreadyUsed;
import ie.universityofgalway.groupnine.domain.auth.ExpiredVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.InvalidVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.TokenAlreadyUsed;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestControllerAdvice(basePackages = "ie.universityofgalway.groupnine.delivery.rest")
public class AuthExceptionHandler {
    private static final AppLogger log = AppLogger.get(AuthExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyUsed.class)
    public ResponseEntity<ApiError> handleEmailUsed(EmailAlreadyUsed ex, WebRequest req) {
        log.info("auth_conflict_email_used", "message", ex.getMessage());
        return toBody(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidVerificationToken.class)
    public ResponseEntity<ApiError> handleInvalidToken(InvalidVerificationToken ex, WebRequest req) {
        log.info("auth_bad_request_invalid_token", "message", ex.getMessage());
        return toBody(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(ExpiredVerificationToken.class)
    public ResponseEntity<ApiError> handleExpired(ExpiredVerificationToken ex, WebRequest req) {
        log.info("auth_gone_expired_token", "message", ex.getMessage());
        return toBody(HttpStatus.GONE, ex.getMessage(), req);
    }

    @ExceptionHandler(TokenAlreadyUsed.class)
    public ResponseEntity<ApiError> handleUsed(TokenAlreadyUsed ex, WebRequest req) {
        log.info("auth_conflict_used_token", "message", ex.getMessage());
        return toBody(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    private ResponseEntity<ApiError> toBody(HttpStatus status, String message, WebRequest request) {
        ApiError body = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", ""),
                List.of()
        );
        return ResponseEntity.status(status).body(body);
    }
}
