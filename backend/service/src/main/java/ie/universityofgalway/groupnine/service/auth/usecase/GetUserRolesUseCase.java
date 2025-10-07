package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;

import java.util.List;
import java.util.Objects;

/**
 * Returns roles for a user.
 */
public class GetUserRolesUseCase {
    private final UserRepositoryPort users;

    public GetUserRolesUseCase(UserRepositoryPort users) {
        this.users = users;
    }

    public Result execute(String userIdRaw) {
        Objects.requireNonNull(userIdRaw, "userId");
        UserId userId = UserId.of(java.util.UUID.fromString(userIdRaw));
        User u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<String> roles = u.getRoles().stream().map(Enum::name).toList();
        return new Result(u.getId().value().toString(), roles);
    }

    public record Result(String userId, List<String> roles) {}
}

