package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.auth.dto.ResetPasswordRequest;
import ie.universityofgalway.groupnine.util.Routes;
import ie.universityofgalway.groupnine.service.auth.usecase.ResetPasswordUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ie.universityofgalway.groupnine.delivery.rest.util.ApiResponse;
import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;
@RestController
@RequestMapping(Routes.AUTH)
@PublicEndpoint
public class ResetPasswordController {
    private final ResetPasswordUseCase useCase;

    @Autowired
    public ResetPasswordController(ResetPasswordUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(path = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Object>> reset(@Valid @RequestBody ResetPasswordRequest req) {
        useCase.execute(req.token, req.password);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
