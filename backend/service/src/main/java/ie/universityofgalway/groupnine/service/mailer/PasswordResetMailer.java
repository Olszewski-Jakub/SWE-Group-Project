package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.PasswordResetEmailJob;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;

import java.util.Objects;

/**
 * Mailer for PASSWORD_RESET emails.
 */
public final class PasswordResetMailer extends BaseMailer<PasswordResetEmailJob> {
    public PasswordResetMailer(IdempotencyPort idempotency,
                               RenderTemplatePort renderer,
                               SendEmailPort sender,
                               String fromEmail,
                               String fromName) {
        super(idempotency, renderer, sender, fromEmail, fromName, PasswordResetMailer.class);
    }

    @Override
    protected void validate(PasswordResetEmailJob job) {
        Objects.requireNonNull(job.templateModel().get("resetLink"), "resetLink required");
    }

    @Override
    public EmailType emailType() {
        return EmailType.PASSWORD_RESET;
    }
}

