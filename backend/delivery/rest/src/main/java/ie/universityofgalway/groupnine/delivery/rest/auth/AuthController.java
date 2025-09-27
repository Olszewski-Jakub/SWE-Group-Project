package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.auth.dto.RegisterRequest;
import ie.universityofgalway.groupnine.delivery.rest.auth.dto.VerifyRequest;
import ie.universityofgalway.groupnine.service.auth.usecase.RegisterUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.VerifyEmailUseCase;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints for registration and email verification.
 * <p>
 * Exposes a minimal API for creating user accounts and confirming
 * ownership of an email address via opaque verification tokens.
 */
@RestController
@RequestMapping(value = "/auth")
public class AuthController {
    private static final AppLogger log = AppLogger.get(AuthController.class);

    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;

    @Autowired
    public AuthController(RegisterUserUseCase registerUserUseCase, VerifyEmailUseCase verifyEmailUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
    }

    /**
     * Register a new user account and dispatch a verification email.
     *
     * @param req validated registration payload
     * @return 201 Created with empty body
     */
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        String maskedEmail = req.email == null ? "" : req.email.replaceAll("(^.).*(@.*$)", "$1***$2");
        log.debug("http_register_request", "email", maskedEmail);
        registerUserUseCase.execute(req.email, req.password, req.firstName, req.lastName);
        log.info("http_register_success", "email", maskedEmail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Confirm a user's email address using an opaque verification token.
     *
     * @param req request containing the opaque token
     * @return 200 OK when verification succeeds
     */
    @PostMapping(path = "/verify-email", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyRequest req) {
        log.debug("http_verify_request");
        verifyEmailUseCase.execute(req.token);
        log.info("http_verify_success");
        return ResponseEntity.ok().build();
    }
}
