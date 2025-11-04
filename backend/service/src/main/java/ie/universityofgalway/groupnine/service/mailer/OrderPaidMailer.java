package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.OrderPaidEmailJob;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;

public final class OrderPaidMailer extends BaseMailer<OrderPaidEmailJob> {
    public OrderPaidMailer(IdempotencyPort idempotency, RenderTemplatePort renderer, SendEmailPort sender, String fromEmail, String fromName) {
        super(idempotency, renderer, sender, fromEmail, fromName, OrderPaidMailer.class);
    }

    @Override protected void validate(OrderPaidEmailJob job) { /* minimal */ }
    @Override public EmailType emailType() { return EmailType.ORDER_PAID; }
}

