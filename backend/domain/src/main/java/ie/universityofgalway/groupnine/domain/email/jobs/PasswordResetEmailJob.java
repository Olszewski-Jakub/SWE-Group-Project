package ie.universityofgalway.groupnine.domain.email.jobs;

import ie.universityofgalway.groupnine.domain.email.AbstractEmailJobBuilder;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.Priority;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Email job for password reset containing the reset link and optional user display name.
 */
public record PasswordResetEmailJob(EmailJobId id, EmailAddress to, Locale locale, Map<String, Object> model,
                                    Instant createdAt, Priority priority) implements EmailJob {
    public PasswordResetEmailJob(EmailJobId id,
                                 EmailAddress to,
                                 Locale locale,
                                 Map<String, Object> model,
                                 Instant createdAt,
                                 Priority priority) {
        this.id = Objects.requireNonNull(id);
        this.to = Objects.requireNonNull(to);
        this.locale = Objects.requireNonNullElse(locale, Locale.ENGLISH);
        this.model = Map.copyOf(Objects.requireNonNull(model));
        this.createdAt = Objects.requireNonNull(createdAt);
        this.priority = Objects.requireNonNullElse(priority, Priority.NORMAL);
        if (!model.containsKey("resetLink")) {
            throw new IllegalArgumentException("resetLink is required");
        }
    }

    public static ToStep builder() {
        return new Builder();
    }

    @Override
    public EmailType type() {
        return EmailType.PASSWORD_RESET;
    }

    @Override
    public Map<String, Object> templateModel() {
        return model;
    }

    public interface ToStep {
        ResetLinkStep to(EmailAddress to);
    }

    public interface ResetLinkStep {
        OptionalStep resetLink(URI link);
    }

    public interface OptionalStep extends AbstractEmailJobBuilder.OptionalStep<OptionalStep, PasswordResetEmailJob> {
        OptionalStep userName(String name);
    }

    public interface BuildStep extends AbstractEmailJobBuilder.BuildStep<PasswordResetEmailJob> {
    }

    public static final class Builder extends AbstractEmailJobBuilder<Builder, PasswordResetEmailJob>
            implements ToStep, ResetLinkStep, OptionalStep, BuildStep {
        private EmailAddress to;
        private String userName;
        private URI resetLink;

        private Builder() {
        }

        @Override
        protected Builder self() { return this; }

        @Override
        public ResetLinkStep to(EmailAddress to) {
            this.to = to;
            return this;
        }

        @Override
        public PasswordResetEmailJob.OptionalStep resetLink(URI link) {
            this.resetLink = link;
            return this;
        }

        @Override
        public PasswordResetEmailJob.OptionalStep userName(String name) {
            this.userName = name;
            return this;
        }

        @Override
        public PasswordResetEmailJob build() {
            Map<String, Object> model = new HashMap<>();
            model.put("resetLink", Objects.requireNonNull(resetLink));
            if (userName != null && !userName.isBlank()) model.put("userName", userName);
            return new PasswordResetEmailJob(id, to, locale, model, createdAt, priority);
        }
    }
}

