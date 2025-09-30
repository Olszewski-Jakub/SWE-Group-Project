package ie.universityofgalway.groupnine.delivery.rest.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for resetting password.
 */
public class ResetPasswordRequest {
    @NotBlank
    @Size(min = 1, max = 512)
    public String token;

    @NotBlank
    @Size(min = 10, max = 200)
    public String password;
}

