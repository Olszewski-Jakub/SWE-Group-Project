package ie.universityofgalway.groupnine.infrastructure.auth.jpa;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleEntityTest {
    @Test
    void gettersSetters() {
        RoleEntity r = new RoleEntity();
        UUID id = UUID.randomUUID();
        r.setId(id);
        r.setName("ADMIN");
        assertEquals(id, r.getId());
        assertEquals("ADMIN", r.getName());
    }
}

