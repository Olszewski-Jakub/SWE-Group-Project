package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.auth.dto.ChangePasswordRequest;
import ie.universityofgalway.groupnine.delivery.rest.support.Routes;
import ie.universityofgalway.groupnine.delivery.rest.support.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.usecase.ChangePasswordUseCase;
import ie.universityofgalway.groupnine.delivery.rest.support.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.AUTH)
public class ChangePasswordController {
    private final ChangePasswordUseCase useCase;
    private final AccessTokenUserResolver userResolver;

    @Autowired
    public ChangePasswordController(ChangePasswordUseCase useCase, AccessTokenUserResolver userResolver) {
        this.useCase = useCase;
        this.userResolver = userResolver;
    }

    @PostMapping(path = "/change-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Object>> changePassword(@Valid @RequestBody ChangePasswordRequest req, HttpServletRequest httpReq) {
        var user = userResolver.requireUser(httpReq);
        useCase.execute(UserId.of(user.getId().value()), req.currentPassword, req.newPassword);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
