package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.PaymentFailedEmailJob;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;

public final class PaymentFailedMailer extends BaseMailer<PaymentFailedEmailJob> {
    public PaymentFailedMailer(IdempotencyPort idempotency, RenderTemplatePort renderer, SendEmailPort sender, String fromEmail, String fromName) {
        super(idempotency, renderer, sender, fromEmail, fromName, PaymentFailedMailer.class);
    }

    @Override protected void validate(PaymentFailedEmailJob job) { /* minimal */ }
    @Override public EmailType emailType() { return EmailType.PAYMENT_FAILED; }
}

