package ie.universityofgalway.groupnine.infrastructure.email;

import ie.universityofgalway.groupnine.domain.user.Email;
import org.junit.jupiter.api.Test;

class LoggingEmailSenderAdapterTest {

    @Test
    void logs_without_throwing_when_enabled_and_disabled() {
        LoggingEmailSenderAdapter enabled = new LoggingEmailSenderAdapter("from@example.com", true);
        enabled.sendVerificationEmail(Email.of("to@example.com"), "http://localhost/verify?token=abc");

        LoggingEmailSenderAdapter disabled = new LoggingEmailSenderAdapter("from@example.com", false);
        disabled.sendVerificationEmail(Email.of("to@example.com"), "http://localhost/verify?token=abc");
    }
}

