package ie.universityofgalway.groupnine.service.auth.port;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.util.Optional;

/**
 * Port for accessing and mutating user aggregates from the application layer.
 */
public interface UserRepositoryPort {
    /**
     * Returns whether a user with the given email exists.
     */
    boolean existsByEmail(Email email);

    /**
     * Finds a user by email, if present.
     */
    Optional<User> findByEmail(Email email);

    /**
     * Finds a user by identifier, if present.
     */
    Optional<User> findById(UserId id);

    /**
     * Persists a new user aggregate.
     */
    User save(User user);

    /**
     * Updates an existing user aggregate.
     */
    User update(User user);
}
