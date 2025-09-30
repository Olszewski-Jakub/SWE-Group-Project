package ie.universityofgalway.groupnine.delivery.rest.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for initiating a password reset.
 */
public class ForgotPasswordRequest {
    @Email
    @NotBlank
    @Size(max = 254)
    public String email;

    // Optional BCP-47 locale tag (e.g., en-IE). Validated len only here.
    @Size(max = 20)
    public String locale;
}

