package ie.universityofgalway.groupnine.delivery.rest.auth.oauth;

import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;
import ie.universityofgalway.groupnine.delivery.rest.auth.dto.TokenResponse;
import ie.universityofgalway.groupnine.util.Routes;
import ie.universityofgalway.groupnine.security.web.TokenCookieFactory;
import ie.universityofgalway.groupnine.service.auth.usecase.GoogleOAuthFlowUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.AUTH + "/oauth/google")
@PublicEndpoint
public class GoogleOAuthController {

    private final TokenCookieFactory cookieFactory;
    private final GoogleOAuthFlowUseCase flow;

    public GoogleOAuthController(TokenCookieFactory cookieFactory, GoogleOAuthFlowUseCase flow) {
        this.cookieFactory = cookieFactory;
        this.flow = flow;
    }

    @GetMapping(path = "/authorize", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthorizeResponse> authorize(@RequestParam(name = "redirect", required = false) String redirect) {
        String url = flow.buildAuthorizationUrl(redirect);
        return ResponseEntity.ok(new AuthorizeResponse(url));
    }

    @GetMapping(path = "/authorize/redirect")
    public ResponseEntity<Void> authorizeRedirect(@RequestParam(name = "redirect", required = false) String redirect) {
        String url = flow.buildAuthorizationUrl(redirect);
        return ResponseEntity.status(302).header(org.springframework.http.HttpHeaders.LOCATION, url).build();
    }

    @GetMapping(path = "/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> callback(@RequestParam(name = "code", required = false) String code,
                                           @RequestParam(name = "state", required = false) String state,
                                           @RequestParam(name = "error", required = false) String error,
                                           HttpServletRequest request) {
        if (error != null && !error.isBlank()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", error));
        }
        String ua = request.getHeader("User-Agent");
        java.net.InetAddress ip = extractClientIp(request);

        GoogleOAuthFlowUseCase.CallbackResult result;
        try {
            result = flow.handleCallback(code, state, ua, ip);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", e.getMessage()));
        }

        ResponseCookie refreshCookie = cookieFactory.buildRefreshCookie(result.refreshToken());
        ResponseCookie accessCookie = cookieFactory.buildAccessCookie(result.accessToken());
        String redirectTarget = result.redirectTarget();
        if (redirectTarget != null) {
            String html = buildLocalStorageRedirectHtml(result.accessToken(), result.refreshToken(), result.expiresInSeconds(), redirectTarget);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                    .header(org.springframework.http.HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .header(org.springframework.http.HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .body(html);
        } else {
            TokenResponse body = new TokenResponse(result.accessToken(), result.expiresInSeconds(), result.refreshToken(), "Bearer");
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .header(org.springframework.http.HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .body(body);
        }
    }

    public static final class AuthorizeResponse {
        private final String authorizationUrl;
        public AuthorizeResponse(String authorizationUrl) { this.authorizationUrl = authorizationUrl; }
        public String getAuthorizationUrl() { return authorizationUrl; }
    }

    private java.net.InetAddress extractClientIp(HttpServletRequest req) {
        try {
            String ip = req.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) {
                String first = ip.split(",")[0].trim();
                return java.net.InetAddress.getByName(first);
            }
            return req.getRemoteAddr() == null ? null : java.net.InetAddress.getByName(req.getRemoteAddr());
        } catch (Exception e) {
            return null;
        }
    }

    private String buildLocalStorageRedirectHtml(String accessToken, String refreshToken, long expiresInSeconds, String redirect) {
        String safeToken = jsonString(accessToken == null ? "" : accessToken);
        String safeRefresh = jsonString(refreshToken == null ? "" : refreshToken);
        String safeRedirect = jsonString(redirect == null ? "/" : redirect);
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>Signing In…</title></head>" +
                "<body><script>(function(){" +
                // Build redirect with hash tokens for first-party storage on the app origin
                "var u=" + safeRedirect + ";" +
                "var h='#accessToken=' + encodeURIComponent(" + safeToken + ") + '&refreshToken=' + encodeURIComponent(" + safeRefresh + ") + '&expiresIn=" + expiresInSeconds + "';" +
                "try{sessionStorage.setItem('postLoginRedirect','1');}catch(e){}" +
                "window.location.replace(u + h);})();</script>" +
                "<noscript>Login completed. Please enable JavaScript to continue.</noscript>" +
                "</body></html>";
    }

    private String jsonString(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
