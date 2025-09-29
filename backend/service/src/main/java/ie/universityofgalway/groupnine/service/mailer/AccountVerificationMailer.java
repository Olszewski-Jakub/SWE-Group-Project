package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.AccountVerificationEmailJob;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;

import java.util.Objects;

/**
 * Mailer responsible for processing {@link AccountVerificationEmailJob} by rendering the
 * verification email template and sending it via the configured transport.
 */
public final class AccountVerificationMailer extends BaseMailer<AccountVerificationEmailJob> {
    public AccountVerificationMailer(IdempotencyPort idempotency,
                                     RenderTemplatePort renderer,
                                     SendEmailPort sender,
                                     String fromEmail,
                                     String fromName) {
        super(idempotency, renderer, sender, fromEmail, fromName, AccountVerificationMailer.class);
    }

    @Override
    protected void validate(AccountVerificationEmailJob job) {
        Objects.requireNonNull(job.templateModel().get("verificationLink"), "verificationLink required");
    }

    @Override
    public EmailType emailType() {
        return EmailType.ACCOUNT_VERIFICATION;
    }
}
