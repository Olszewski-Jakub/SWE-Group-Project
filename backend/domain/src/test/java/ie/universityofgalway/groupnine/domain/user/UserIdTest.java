package ie.universityofgalway.groupnine.domain.user;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserIdTest {
    @Test
    void of_validates_and_compares() {
        assertThrows(IllegalArgumentException.class, () -> UserId.of(null));
        UUID id = UUID.randomUUID();
        UserId a = UserId.of(id);
        UserId b = UserId.of(id);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(id.toString(), a.toString());
        assertEquals(id, a.value());
    }
}

