package ie.universityofgalway.groupnine.domain.user;

import java.time.Instant;
import java.util.Objects;

/**
 * User aggregate root representing an account in the system.
 */
public class User {
    private final UserId id;
    private final Email email;
    private final String firstName;
    private final String lastName;
    private final UserStatus status;
    private final boolean emailVerified;
    private final String passwordHash; // may be null for social-only accounts
    private final Instant createdAt;
    private final Instant updatedAt;
    private final java.util.Set<Role> roles;

    public User(UserId id,
                Email email,
                String firstName,
                String lastName,
                UserStatus status,
                boolean emailVerified,
                String passwordHash,
                Instant createdAt,
                Instant updatedAt) {
        this(id, email, firstName, lastName, status, emailVerified, passwordHash, createdAt, updatedAt, java.util.Set.of());
    }

    public User(UserId id,
                Email email,
                String firstName,
                String lastName,
                UserStatus status,
                boolean emailVerified,
                String passwordHash,
                Instant createdAt,
                Instant updatedAt,
                java.util.Set<Role> roles) {
        this.id = Objects.requireNonNull(id, "id");
        this.email = Objects.requireNonNull(email, "email");
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = Objects.requireNonNull(status, "status");
        this.emailVerified = emailVerified;
        this.passwordHash = passwordHash;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.roles = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(roles == null ? java.util.Set.of() : roles));
    }

    public static User createNew(Email email,
                                 String firstName,
                                 String lastName,
                                 String passwordHash,
                                 Instant now) {
        return new User(
                UserId.newId(),
                email,
                firstName,
                lastName,
                UserStatus.ACTIVE,
                false,
                passwordHash,
                now,
                now,
                java.util.Set.of(Role.CUSTOMER)
        );
    }

    public User markEmailVerified(Instant now) {
        return new User(id, email, firstName, lastName, status, true, passwordHash, createdAt, now, roles);
    }

    public UserId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public java.util.Set<Role> getRoles() {
        return roles;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public User withRoleAdded(Role role, Instant now) {
        java.util.Set<Role> newRoles = new java.util.HashSet<>(roles);
        newRoles.add(role);
        return new User(id, email, firstName, lastName, status, emailVerified, passwordHash, createdAt, now, newRoles);
    }

    public User withRoleRemoved(Role role, Instant now) {
        java.util.Set<Role> newRoles = new java.util.HashSet<>(roles);
        newRoles.remove(role);
        return new User(id, email, firstName, lastName, status, emailVerified, passwordHash, createdAt, now, newRoles);
    }
}
