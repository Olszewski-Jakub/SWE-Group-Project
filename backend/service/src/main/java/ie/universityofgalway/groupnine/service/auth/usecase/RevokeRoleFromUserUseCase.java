package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;

import java.time.Instant;
import java.util.Objects;

/**
 * Idempotently revokes a role from a user.
 */
public class RevokeRoleFromUserUseCase {
    private final UserRepositoryPort users;
    private final ClockPort clock;

    public RevokeRoleFromUserUseCase(UserRepositoryPort users, ClockPort clock) {
        this.users = users;
        this.clock = clock;
    }

    public AssignRoleToUserUseCase.Result execute(String userIdRaw, String roleName) {
        Objects.requireNonNull(userIdRaw, "userId");
        Objects.requireNonNull(roleName, "role");
        Role role = Role.valueOf(roleName.toUpperCase());
        UserId userId = UserId.of(java.util.UUID.fromString(userIdRaw));
        User u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!u.hasRole(role)) {
            return new AssignRoleToUserUseCase.Result(u.getId().value().toString(), u.getRoles().stream().map(Enum::name).toList());
        }
        Instant now = clock.now();
        User updated = u.withRoleRemoved(role, now);
        updated = users.update(updated);
        return new AssignRoleToUserUseCase.Result(updated.getId().value().toString(), updated.getRoles().stream().map(Enum::name).toList());
    }
}

