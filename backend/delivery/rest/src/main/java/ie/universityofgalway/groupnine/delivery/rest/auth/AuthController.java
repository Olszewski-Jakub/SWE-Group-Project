package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.auth.dto.LoginRequest;
import ie.universityofgalway.groupnine.delivery.rest.auth.dto.RegisterRequest;
import ie.universityofgalway.groupnine.delivery.rest.auth.dto.TokenResponse;
import ie.universityofgalway.groupnine.delivery.rest.auth.dto.VerifyRequest;
import ie.universityofgalway.groupnine.delivery.rest.util.Routes;
import ie.universityofgalway.groupnine.domain.auth.exception.InvalidRefreshToken;
import ie.universityofgalway.groupnine.security.config.props.AuthProps;
import ie.universityofgalway.groupnine.service.auth.usecase.LoginUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutAllUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.LogoutUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RefreshUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RegisterUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.VerifyEmailUseCase;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

/**
 * Authentication endpoints for registration and email verification.
 * <p>
 * Exposes a minimal API for creating user accounts and confirming
 * ownership of an email address via opaque verification tokens.
 */
@RestController
@RequestMapping(value = Routes.AUTH)
@PublicEndpoint
public class AuthController {
    private static final AppLogger log = AppLogger.get(AuthController.class);

    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final LogoutAllUseCase logoutAllUseCase;
    private final AuthProps authProps;

    @Autowired
    public AuthController(RegisterUserUseCase registerUserUseCase,
                          VerifyEmailUseCase verifyEmailUseCase,
                          LoginUseCase loginUseCase,
                          RefreshUseCase refreshUseCase,
                          LogoutUseCase logoutUseCase,
                          LogoutAllUseCase logoutAllUseCase,
                          AuthProps authProps) {
        this.registerUserUseCase = registerUserUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
        this.logoutAllUseCase = logoutAllUseCase;
        this.authProps = authProps;
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

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Authenticate a user and create a refresh-token session.
     * Returns an access token and refresh token; also sets the refresh token cookie.
     */
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest httpReq) {
        String ua = httpReq.getHeader("User-Agent");
        InetAddress ip = extractClientIp(httpReq);
        log.info("ip_address", "ip", ip == null ? "unknown" : ip.getHostAddress());
        LoginUseCase.Result result = loginUseCase.execute(req.email, req.password, ua, ip);

        ResponseCookie cookie = buildRefreshCookie(result.refreshToken());
        TokenResponse body = new TokenResponse(result.accessToken(), result.expiresInSeconds(), result.refreshToken(), "Bearer");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(body);
    }

    @PostMapping(path = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    /**
     * Rotate a refresh token and return a new access token.
     * Accepts the token from header {@code X-Refresh-Token} or the refresh cookie.
     */
    public ResponseEntity<TokenResponse> refresh(
            @RequestHeader(name = "X-Refresh-Token", required = false) String headerToken,
            HttpServletRequest httpReq
    ) {
        String cookieToken = extractRefreshFromCookie(httpReq);
        String token = (headerToken != null && !headerToken.isBlank()) ? headerToken : cookieToken;
        if (token == null || token.isBlank()) {
            throw new InvalidRefreshToken("Missing refresh token");
        }
        String ua = httpReq.getHeader("User-Agent");
        InetAddress ip = extractClientIp(httpReq);
        RefreshUseCase.Result result = refreshUseCase.execute(token, ua, ip);
        ResponseCookie cookie = buildRefreshCookie(result.refreshToken());
        TokenResponse body = new TokenResponse(result.accessToken(), result.expiresInSeconds(), result.refreshToken(), "Bearer");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(body);
    }

    @PostMapping(path = "/logout")
    /**
     * Revoke the current session identified by the provided refresh token (header or cookie).
     * Clears the refresh token cookie.
     */
    public ResponseEntity<Void> logout(@RequestHeader(name = "X-Refresh-Token", required = false) String headerToken,
                                       HttpServletRequest httpReq) {
        String cookieToken = extractRefreshFromCookie(httpReq);
        String token = (headerToken != null && !headerToken.isBlank()) ? headerToken : cookieToken;
        if (token != null && !token.isBlank()) {
            logoutUseCase.execute(token);
        }
        ResponseCookie cleared = clearRefreshCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cleared.toString()).build();
    }

    @PostMapping(path = "/logout-all")
    /**
     * Revoke all active sessions for the user owning the provided refresh token (header or cookie).
     * Clears the refresh token cookie.
     */
    public ResponseEntity<Void> logoutAll(@RequestHeader(name = "X-Refresh-Token", required = false) String headerToken,
                                          HttpServletRequest httpReq) {
        String cookieToken = extractRefreshFromCookie(httpReq);
        String token = (headerToken != null && !headerToken.isBlank()) ? headerToken : cookieToken;
        if (token != null && !token.isBlank()) {
            logoutAllUseCase.execute(token);
        }
        ResponseCookie cleared = clearRefreshCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cleared.toString()).build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        long maxAge = java.time.Duration.ofDays(authProps.getRefreshTtlDays()).getSeconds();
        return ResponseCookie.from(authProps.getRefreshCookieName(), token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(Routes.AUTH + "/refresh")
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(authProps.getRefreshCookieName(), "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(Routes.AUTH + "/refresh")
                .maxAge(0)
                .build();
    }

    private InetAddress extractClientIp(HttpServletRequest req) {
        try {
            String ip = req.getHeader("X-Forwarded-For");

            if (ip != null && !ip.isBlank()) {
                log.info("X-Forwarded-For: {} " + ip);
                String first = ip.split(",")[0].trim();
                return InetAddress.getByName(first);
            }
            return req.getRemoteAddr() == null ? null : InetAddress.getByName(req.getRemoteAddr());
        } catch (Exception e) {
            return null;
        }
    }

    private String extractRefreshFromCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (authProps.getRefreshCookieName().equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
