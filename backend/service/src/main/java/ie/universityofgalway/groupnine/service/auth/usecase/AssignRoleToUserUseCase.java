package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;

import java.time.Instant;
import java.util.Objects;

/**
 * Idempotently assigns a role to a user.
 */
public class AssignRoleToUserUseCase {
    private final UserRepositoryPort users;
    private final ClockPort clock;

    public AssignRoleToUserUseCase(UserRepositoryPort users, ClockPort clock) {
        this.users = users;
        this.clock = clock;
    }

    public Result execute(String userIdRaw, String roleName) {
        Objects.requireNonNull(userIdRaw, "userId");
        Objects.requireNonNull(roleName, "role");
        Role role = Role.valueOf(roleName.toUpperCase());
        UserId userId = UserId.of(java.util.UUID.fromString(userIdRaw));
        User u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (u.hasRole(role)) {
            return new Result(u.getId().value().toString(), u.getRoles().stream().map(Enum::name).toList());
        }
        Instant now = clock.now();
        User updated = u.withRoleAdded(role, now);
        updated = users.update(updated);
        return new Result(updated.getId().value().toString(), updated.getRoles().stream().map(Enum::name).toList());
    }

    public record Result(String userId, java.util.List<String> roles) {}
}

