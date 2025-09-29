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

/**
 * Email job sent after successful verification welcoming the user to the Coffee Shop.
 */
public record WelcomeEmailJob(EmailJobId id, EmailAddress to, Locale locale, Map<String, Object> model,
                              Instant createdAt, Priority priority) implements EmailJob {
    public WelcomeEmailJob(EmailJobId id,
                           EmailAddress to,
                           Locale locale,
                           Map<String, Object> model,
                           Instant createdAt,
                           Priority priority) {
        this.id = Objects.requireNonNull(id);
        this.to = Objects.requireNonNull(to);
        this.locale = Objects.requireNonNullElse(locale, Locale.ENGLISH);
        this.model = Map.copyOf(Objects.requireNonNullElse(model, Map.of()));
        this.createdAt = Objects.requireNonNull(createdAt);
        this.priority = Objects.requireNonNullElse(priority, Priority.NORMAL);
    }

    public static ToStep builder() {
        return new Builder();
    }

    @Override
    public EmailType type() {
        return EmailType.WELCOME;
    }

    @Override
    public Map<String, Object> templateModel() {
        return model;
    }

    /**
     * First required step: destination address.
     */
    public interface ToStep {
        OptionalStep to(EmailAddress to);
    }

    /**
     * Optional fields including locale, id, createdAt, priority and userName.
     */
    public interface OptionalStep extends AbstractEmailJobBuilder.OptionalStep<OptionalStep, WelcomeEmailJob> {
        OptionalStep userName(String name);
    }

    /**
     * Terminal build step.
     */
    public interface BuildStep extends AbstractEmailJobBuilder.BuildStep<WelcomeEmailJob> {
    }

    public static final class Builder extends AbstractEmailJobBuilder<Builder, WelcomeEmailJob>
            implements ToStep, OptionalStep, BuildStep {
        private EmailAddress to;
        private String userName;

        private Builder() {
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public WelcomeEmailJob.OptionalStep to(EmailAddress to) {
            this.to = to;
            return this;
        }

        @Override
        public WelcomeEmailJob.OptionalStep userName(String name) {
            this.userName = name;
            return this;
        }

        @Override
        public WelcomeEmailJob build() {
            Map<String, Object> model = new HashMap<>();
            if (userName != null && !userName.isBlank()) model.put("userName", userName);
            return new WelcomeEmailJob(id, to, locale, model, createdAt, priority);
        }
    }
}
