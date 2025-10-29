package ie.universityofgalway.groupnine.infrastructure.email;

import ie.universityofgalway.groupnine.infrastructure.email.adapter.MailjetEmailSenderAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailjetEmailSenderAdapterTest {

    @Test
    void constructorThrowsWhenMissingCredentials() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> new MailjetEmailSenderAdapter("", ""));
        assertTrue(ex.getMessage().contains("Mailjet API credentials"));
    }
}

