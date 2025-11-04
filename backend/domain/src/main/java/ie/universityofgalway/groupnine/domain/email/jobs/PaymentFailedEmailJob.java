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

public record PaymentFailedEmailJob(EmailJobId id, EmailAddress to, Locale locale, Map<String, Object> model,
                                    Instant createdAt, Priority priority) implements EmailJob {
    public PaymentFailedEmailJob(EmailJobId id, EmailAddress to, Locale locale, Map<String, Object> model, Instant createdAt, Priority priority) {
        this.id = Objects.requireNonNull(id);
        this.to = Objects.requireNonNull(to);
        this.locale = Objects.requireNonNullElse(locale, Locale.ENGLISH);
        this.model = Map.copyOf(Objects.requireNonNullElse(model, Map.of()));
        this.createdAt = Objects.requireNonNull(createdAt);
        this.priority = Objects.requireNonNullElse(priority, Priority.NORMAL);
    }

    public static ToStep builder() { return new Builder(); }

    @Override public EmailType type() { return EmailType.PAYMENT_FAILED; }
    @Override public Map<String, Object> templateModel() { return model; }

    public interface ToStep { OptionalStep to(EmailAddress to); }
    public interface OptionalStep extends AbstractEmailJobBuilder.OptionalStep<OptionalStep, PaymentFailedEmailJob> {
        OptionalStep orderId(String orderId);
        OptionalStep reason(String reason);
        OptionalStep userName(String name);
    }
    public interface BuildStep extends AbstractEmailJobBuilder.BuildStep<PaymentFailedEmailJob> {}

    public static final class Builder extends AbstractEmailJobBuilder<Builder, PaymentFailedEmailJob>
            implements ToStep, OptionalStep, BuildStep {
        private EmailAddress to;
        private String orderId;
        private String reason;
        private String userName;

        @Override protected Builder self() { return this; }
        @Override public PaymentFailedEmailJob.OptionalStep to(EmailAddress to) { this.to = to; return this; }
        @Override public PaymentFailedEmailJob.OptionalStep orderId(String orderId) { this.orderId = orderId; return this; }
        @Override public PaymentFailedEmailJob.OptionalStep reason(String reason) { this.reason = reason; return this; }
        @Override public PaymentFailedEmailJob.OptionalStep userName(String name) { this.userName = name; return this; }
        @Override public PaymentFailedEmailJob build() {
            Map<String, Object> model = new HashMap<>();
            if (orderId != null) model.put("orderId", orderId);
            if (reason != null) model.put("reason", reason);
            if (userName != null) model.put("userName", userName);
            return new PaymentFailedEmailJob(id, to, locale, model, createdAt, priority);
        }
    }
}

