package ie.universityofgalway.groupnine.delivery.rest.auth.dto;

import java.util.List;
import java.util.Objects;

/**
 * Response payload for the current authenticated user details.
 */
public final class MeResponse {
    private final String id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final List<String> roles;

    public MeResponse(String id, String email, String firstName, String lastName, List<String> roles) {
        this.id = Objects.requireNonNull(id, "id");
        this.email = Objects.requireNonNull(email, "email");
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles == null ? java.util.List.of() : java.util.List.copyOf(roles);
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<String> getRoles() {
        return roles;
    }
}
