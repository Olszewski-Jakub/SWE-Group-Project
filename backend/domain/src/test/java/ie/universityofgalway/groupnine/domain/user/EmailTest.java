package ie.universityofgalway.groupnine.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {
    @Test
    void of_validates_and_normalizes() {
        assertThrows(IllegalArgumentException.class, () -> Email.of(null));
        assertThrows(IllegalArgumentException.class, () -> Email.of(" "));
        assertThrows(IllegalArgumentException.class, () -> Email.of("invalid"));

        Email e = Email.of(" User@Example.com ");
        assertEquals("user@example.com", e.value());
        assertEquals(e, Email.of("user@example.com"));
        assertEquals(e.hashCode(), Email.of("user@example.com").hashCode());
        assertEquals("user@example.com", e.toString());
    }
}

