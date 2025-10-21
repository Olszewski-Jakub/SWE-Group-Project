package ie.universityofgalway.groupnine.domain.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressTest {
    @Test
    void validates_and_trims() {
        assertThrows(NullPointerException.class, () -> new EmailAddress(null));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(""));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("no-at"));
        EmailAddress e = new EmailAddress(" user@example.com ");
        assertEquals("user@example.com", e.value());
    }
}

