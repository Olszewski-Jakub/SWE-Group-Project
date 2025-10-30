package ie.universityofgalway.groupnine.delivery.webhook;

import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;
import ie.universityofgalway.groupnine.service.payments.webhook.port.StripeEventParserPort;
import ie.universityofgalway.groupnine.service.payments.webhook.usecase.ProcessStripeWebhookUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@PublicEndpoint
public class StripeWebhookController {

    private final ProcessStripeWebhookUseCase useCase;

    public StripeWebhookController(ProcessStripeWebhookUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Receives Stripe webhook events. Verifies signature and delegates processing to the use case.
     * Returns 200 OK for accepted/ignored events, 400 for invalid signatures.
     */
    @PostMapping
    public ResponseEntity<String> receive(HttpServletRequest request) throws IOException {
        String payload = readBody(request);
        String signature = request.getHeader("Stripe-Signature");
        try {
            useCase.execute(payload, signature);
            return ResponseEntity.ok("ok");
        } catch (StripeEventParserPort.InvalidSignatureException e) {
            return ResponseEntity.status(400).body("invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(200).body("ignored");
        }
    }

    private static String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            char[] buf = new char[2048];
            int n;
            while ((n = reader.read(buf)) > 0) {
                sb.append(buf, 0, n);
            }
        }
        return sb.toString();
    }
}
