package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.auth.dto.ForgotPasswordRequest;
import ie.universityofgalway.groupnine.delivery.rest.support.Routes;
import ie.universityofgalway.groupnine.service.auth.usecase.RequestPasswordResetUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ie.universityofgalway.groupnine.delivery.rest.support.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.InetAddress;

@RestController
@RequestMapping(Routes.AUTH)
public class ForgotPasswordController {

    private final RequestPasswordResetUseCase useCase;

    @Autowired
    public ForgotPasswordController(RequestPasswordResetUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(path = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Message>> forgot(@Valid @RequestBody ForgotPasswordRequest req, HttpServletRequest httpReq) {
        InetAddress ip = extractClientIp(httpReq);
        useCase.execute(req.email, req.locale, ip);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(new Message("If an account exists for that email, you will receive a password reset email shortly.")));
    }

    private InetAddress extractClientIp(HttpServletRequest req) {
        try {
            String ip = req.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) {
                String first = ip.split(",")[0].trim();
                return InetAddress.getByName(first);
            }
            return req.getRemoteAddr() == null ? null : InetAddress.getByName(req.getRemoteAddr());
        } catch (Exception e) {
            return null;
        }
    }

    public record Message(String message) {}
}
