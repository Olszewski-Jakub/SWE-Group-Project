package ie.universityofgalway.groupnine.delivery.rest.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration payload for creating a new user.
 */
public class RegisterRequest {
    /**
     * User email address; validated and normalized downstream.
     */
    @Email
    @NotBlank
    @Size(max = 254)
    public String email;

    /**
     * Raw password; must meet minimum length. Additional policy may be enforced by the service.
     */
    @NotBlank
    @Size(min = 10, max = 128)
    public String password;

    /**
     * First name (display purposes).
     */
    @NotBlank
    @Size(min = 1, max = 100)
    public String firstName;

    /**
     * Last name (display purposes).
     */
    @NotBlank
    @Size(min = 1, max = 100)
    public String lastName;
}
