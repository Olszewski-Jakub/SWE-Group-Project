package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.support.ApiError;
import ie.universityofgalway.groupnine.domain.auth.*;
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

    @ExceptionHandler(InvalidCredentials.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentials ex, WebRequest req) {
        log.info("auth_unauthorized_invalid_credentials", "message", ex.getMessage());
        return toBody(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(UserNotVerified.class)
    public ResponseEntity<ApiError> handleNotVerified(UserNotVerified ex, WebRequest req) {
        log.info("auth_forbidden_not_verified", "message", ex.getMessage());
        return toBody(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(UserLocked.class)
    public ResponseEntity<ApiError> handleLocked(UserLocked ex, WebRequest req) {
        log.info("auth_locked_user", "message", ex.getMessage());
        return toBody(HttpStatus.LOCKED, ex.getMessage(), req);
    }

    @ExceptionHandler({InvalidRefreshToken.class, ExpiredRefreshToken.class})
    public ResponseEntity<ApiError> handleRefreshInvalid(RuntimeException ex, WebRequest req) {
        log.info("auth_unauthorized_refresh", "message", ex.getMessage());
        return toBody(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(RefreshReuseDetected.class)
    public ResponseEntity<ApiError> handleReuse(RefreshReuseDetected ex, WebRequest req) {
        log.info("auth_conflict_refresh_reuse", "message", ex.getMessage());
        return toBody(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(TooManyAttempts.class)
    public ResponseEntity<ApiError> handleTooMany(TooManyAttempts ex, WebRequest req) {
        log.info("auth_rate_limited", "retry_after_seconds", ex.getRetryAfterSeconds());
        ApiError body = new ApiError(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                ex.getMessage(),
                req.getDescription(false).replace("uri=", ""),
                java.util.List.of("retryAfterSeconds=" + ex.getRetryAfterSeconds())
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(org.springframework.http.HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(body);
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
