package ie.universityofgalway.groupnine.delivery.rest.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @Email
    @NotBlank
    @Size(max = 254)
    public String email;

    @NotBlank
    @Size(min = 1, max = 256)
    public String password;
}

