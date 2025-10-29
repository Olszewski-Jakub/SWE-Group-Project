package ie.universityofgalway.groupnine.delivery.rest.dev;

import ie.universityofgalway.groupnine.service.session.usecase.GetSessionChainUseCase;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;
/**
 * Development-only endpoints for visualizing refresh-token session chains.
 */
@RestController
@RequestMapping("/dev/sessions")
@PublicEndpoint
public class DevSessionController {

    private final GetSessionChainUseCase getSessionChainUseCase;

    public DevSessionController(GetSessionChainUseCase getSessionChainUseCase) {
        this.getSessionChainUseCase = getSessionChainUseCase;
    }

    /**
     * Visualize the rotation chain starting from a sessionId or a refreshToken.
     * Exactly one of sessionId or refreshToken must be provided.
     */
    @GetMapping("/chain")
    public ResponseEntity<?> chain(@RequestParam(name = "sessionId", required = false) UUID sessionId,
                                   @RequestParam(name = "refreshToken", required = false) String refreshToken) {
        if ((sessionId == null && (refreshToken == null || refreshToken.isBlank())) ||
                (sessionId != null && refreshToken != null)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Provide exactly one of sessionId or refreshToken"
            ));
        }
        List<GetSessionChainUseCase.SessionNode> nodes =
                sessionId != null ? getSessionChainUseCase.bySessionId(sessionId)
                        : getSessionChainUseCase.byRefreshToken(refreshToken);
        return ResponseEntity.ok(Map.of("nodes", nodes));
    }

}
