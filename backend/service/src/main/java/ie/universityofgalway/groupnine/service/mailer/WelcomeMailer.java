package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.WelcomeEmailJob;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;

/**
 * Mailer responsible for sending post-verification welcome emails to users.
 */
public final class WelcomeMailer extends BaseMailer<WelcomeEmailJob> {
    public WelcomeMailer(IdempotencyPort idempotency,
                         RenderTemplatePort renderer,
                         SendEmailPort sender,
                         String fromEmail,
                         String fromName) {
        super(idempotency, renderer, sender, fromEmail, fromName, WelcomeMailer.class);
    }

    @Override
    protected void validate(WelcomeEmailJob job) {
        // No required model fields
    }

    @Override
    public EmailType emailType() {
        return EmailType.WELCOME;
    }
}
