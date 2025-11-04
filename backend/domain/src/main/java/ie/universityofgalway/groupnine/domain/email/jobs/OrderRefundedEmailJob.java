package ie.universityofgalway.groupnine.domain.email.jobs;

import ie.universityofgalway.groupnine.domain.email.AbstractEmailJobBuilder;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.Priority;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public record OrderRefundedEmailJob(EmailJobId id, EmailAddress to, Locale locale, Map<String, Object> model,
                                    Instant createdAt, Priority priority) implements EmailJob {
    public OrderRefundedEmailJob(EmailJobId id, EmailAddress to, Locale locale, Map<String, Object> model, Instant createdAt, Priority priority) {
        this.id = Objects.requireNonNull(id);
        this.to = Objects.requireNonNull(to);
        this.locale = Objects.requireNonNullElse(locale, Locale.ENGLISH);
        this.model = Map.copyOf(Objects.requireNonNullElse(model, Map.of()));
        this.createdAt = Objects.requireNonNull(createdAt);
        this.priority = Objects.requireNonNullElse(priority, Priority.NORMAL);
    }

    public static ToStep builder() { return new Builder(); }

    @Override public EmailType type() { return EmailType.ORDER_REFUNDED; }
    @Override public Map<String, Object> templateModel() { return model; }

    public interface ToStep { OptionalStep to(EmailAddress to); }
    public interface OptionalStep extends AbstractEmailJobBuilder.OptionalStep<OptionalStep, OrderRefundedEmailJob> {
        OptionalStep orderId(String orderId);
        OptionalStep amountMinor(long amountMinor);
        OptionalStep currency(String currency);
        OptionalStep userName(String name);
    }
    public interface BuildStep extends AbstractEmailJobBuilder.BuildStep<OrderRefundedEmailJob> {}

    public static final class Builder extends AbstractEmailJobBuilder<Builder, OrderRefundedEmailJob>
            implements ToStep, OptionalStep, BuildStep {
        private EmailAddress to;
        private String orderId;
        private Long amountMinor;
        private String currency;
        private String userName;

        @Override protected Builder self() { return this; }
        @Override public OrderRefundedEmailJob.OptionalStep to(EmailAddress to) { this.to = to; return this; }
        @Override public OrderRefundedEmailJob.OptionalStep orderId(String orderId) { this.orderId = orderId; return this; }
        @Override public OrderRefundedEmailJob.OptionalStep amountMinor(long amountMinor) { this.amountMinor = amountMinor; return this; }
        @Override public OrderRefundedEmailJob.OptionalStep currency(String currency) { this.currency = currency; return this; }
        @Override public OrderRefundedEmailJob.OptionalStep userName(String name) { this.userName = name; return this; }
        @Override public OrderRefundedEmailJob build() {
            Map<String, Object> model = new HashMap<>();
            if (orderId != null) model.put("orderId", orderId);
            if (amountMinor != null) model.put("amountMinor", amountMinor);
            if (currency != null) model.put("currency", currency);
            if (userName != null) model.put("userName", userName);
            return new OrderRefundedEmailJob(id, to, locale, model, createdAt, priority);
        }
    }
}

