package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.domain.auth.EmailAlreadyUsed;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.UserEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.UserJpaRepository;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA-backed implementation of the user repository port.
 * <p>
 * Translates unique email constraint violations into a domain-level
 * {@link ie.universityofgalway.groupnine.domain.auth.EmailAlreadyUsed}
 * to provide consistent error handling at the service layer.
 */
@Component
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository repo;

    @Autowired
    public JpaUserRepositoryAdapter(UserJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repo.existsByEmail(email.value());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return repo.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return repo.findById(id.value()).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        try {
            UserEntity entity = toEntity(user);
            UserEntity saved = repo.save(entity);
            return toDomain(saved);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyUsed("Email already in use");
        }
    }

    @Override
    public User update(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    private UserEntity toEntity(User user) {
        UserEntity e = new UserEntity();
        e.setId(user.getId().value());
        e.setEmail(user.getEmail().value());
        e.setFirstName(user.getFirstName());
        e.setLastName(user.getLastName());
        e.setEmailVerified(user.isEmailVerified());
        e.setPasswordHash(user.getPasswordHash());
        e.setCreatedAt(user.getCreatedAt());
        e.setUpdatedAt(user.getUpdatedAt());
        return e;
    }

    private User toDomain(UserEntity e) {
        return new User(
                UserId.of(e.getId()),
                Email.of(e.getEmail()),
                e.getFirstName(),
                e.getLastName(),
                e.getStatus() == null ? UserStatus.ACTIVE : UserStatus.valueOf(e.getStatus()),
                e.isEmailVerified(),
                e.getPasswordHash(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
