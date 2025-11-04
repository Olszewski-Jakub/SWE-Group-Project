package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.OrderRefundedEmailJob;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;

public final class OrderRefundedMailer extends BaseMailer<OrderRefundedEmailJob> {
    public OrderRefundedMailer(IdempotencyPort idempotency, RenderTemplatePort renderer, SendEmailPort sender, String fromEmail, String fromName) {
        super(idempotency, renderer, sender, fromEmail, fromName, OrderRefundedMailer.class);
    }

    @Override protected void validate(OrderRefundedEmailJob job) { /* minimal */ }
    @Override public EmailType emailType() { return EmailType.ORDER_REFUNDED; }
}

