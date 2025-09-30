package ie.universityofgalway.groupnine.delivery.rest.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for changing password of the authenticated user.
 */
public class ChangePasswordRequest {
    @NotBlank
    public String currentPassword;

    @NotBlank
    @Size(min = 10, max = 200)
    public String newPassword;
}

