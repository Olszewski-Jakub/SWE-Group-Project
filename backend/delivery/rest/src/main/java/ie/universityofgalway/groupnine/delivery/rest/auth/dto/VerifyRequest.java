package ie.universityofgalway.groupnine.delivery.rest.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for email verification.
 */
public class VerifyRequest {
    /**
     * Opaque verification token received by the user over email.
     */
    @NotBlank
    @Size(min = 1, max = 512)
    public String token;
}
