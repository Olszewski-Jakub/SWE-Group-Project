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
    @Test
    void equals_and_hashCode_and_toString_behave_correctly() {
        Instant now = Instant.now();
        UserId id1 = UserId.newId();

        User user1 = new User(id1, Email.of("a@b.com"), "A", "B", UserStatus.ACTIVE, false, "h", now, now, Set.of());
        User user2 = new User(id1, Email.of("a@b.com"), "A", "B", UserStatus.ACTIVE, false, "h", now, now, Set.of());
        User user3 = new User(UserId.newId(), Email.of("c@d.com"), "C", "D", UserStatus.LOCKED, true, "h2", now, now, Set.of());

        // Test equals and hashCode
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());

        // Test toString to ensure it doesn't crash and contains key info
        assertTrue(user1.toString().contains(id1.toString()));
        assertTrue(user1.toString().contains("a@b.com"));
    }
}

