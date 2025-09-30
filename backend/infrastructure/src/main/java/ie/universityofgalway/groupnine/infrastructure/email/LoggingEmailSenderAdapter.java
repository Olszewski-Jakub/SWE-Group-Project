package ie.universityofgalway.groupnine.infrastructure.email;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.service.auth.port.EmailSenderPort;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoggingEmailSenderAdapter implements EmailSenderPort {
    private static final AppLogger log = AppLogger.get(LoggingEmailSenderAdapter.class);

    private final String from;
    private final boolean enabled;

    @Autowired
    public LoggingEmailSenderAdapter(
            @Value("${app.email.from:no-reply@example.com}") String from,
            @Value("${app.email.enabled:true}") boolean enabled
    ) {
        this.from = from;
        this.enabled = enabled;
    }

    @Override
    public void sendVerificationEmail(Email to, String verifyUrl) {
        if (!enabled) {
            log.info("dev_email_disabled would_send_verification_email", "to", to.value(), "verifyUrl", verifyUrl);
            return;
        }
        log.info("sending_verification_email", "from", from, "to", to.value(), "verifyUrl", verifyUrl);
    }
}
