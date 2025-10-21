package ie.universityofgalway.groupnine.domain.user;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void createNew_setsDefaults_andCustomerRole() {
        Instant now = Instant.now();
        User u = User.createNew(Email.of("a@b.com"), "A", "B", "hash", now);
        assertNotNull(u.getId());
        assertEquals(Email.of("a@b.com"), u.getEmail());
        assertEquals(UserStatus.ACTIVE, u.getStatus());
        assertFalse(u.isEmailVerified());
        assertEquals("hash", u.getPasswordHash());
        assertEquals(now, u.getCreatedAt());
        assertEquals(now, u.getUpdatedAt());
        assertTrue(u.getRoles().contains(Role.CUSTOMER));
    }

    @Test
    void role_mutations_are_immutable() {
        Instant now = Instant.now();
        User u = new User(
                UserId.newId(),
                Email.of("x@y.com"),
                "X", "Y",
                UserStatus.ACTIVE,
                false,
                null,
                now,
                now,
                Set.of(Role.CUSTOMER)
        );

        User withAdmin = u.withRoleAdded(Role.ADMIN, now.plusSeconds(1));
        assertTrue(withAdmin.hasRole(Role.ADMIN));
        assertTrue(withAdmin.hasRole(Role.CUSTOMER));
        assertFalse(u.hasRole(Role.ADMIN)); // original unchanged

        User removedCustomer = withAdmin.withRoleRemoved(Role.CUSTOMER, now.plusSeconds(2));
        assertFalse(removedCustomer.hasRole(Role.CUSTOMER));
        assertTrue(removedCustomer.hasRole(Role.ADMIN));
    }

    @Test
    void markEmailVerified_returnsNewInstance() {
        Instant now = Instant.now();
        User u = User.createNew(Email.of("a@b.com"), "A", "B", "h", now);
        User v = u.markEmailVerified(now.plusSeconds(5));
        assertFalse(u.isEmailVerified());
        assertTrue(v.isEmailVerified());
        assertNotEquals(u.getUpdatedAt(), v.getUpdatedAt());
        assertEquals(u.getId(), v.getId());
    }
}

