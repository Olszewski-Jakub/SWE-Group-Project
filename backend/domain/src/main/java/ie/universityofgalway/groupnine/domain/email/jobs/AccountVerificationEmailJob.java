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
 * Email job for account verification containing the verification link and optional name.
 * Uses a staged builder to enforce required fields.
 */
public record AccountVerificationEmailJob(EmailJobId id, EmailAddress to, Locale locale, Map<String, Object> model,
                                          Instant createdAt, Priority priority) implements EmailJob {
    public AccountVerificationEmailJob(EmailJobId id,
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
        if (!model.containsKey("verificationLink")) {
            throw new IllegalArgumentException("verificationLink is required");
        }
    }

    public static ToStep builder() {
        return new Builder();
    }

    @Override
    public EmailType type() {
        return EmailType.ACCOUNT_VERIFICATION;
    }

    @Override
    public Map<String, Object> templateModel() {
        return model;
    }

    /**
     * First required step: destination address.
     */
    public interface ToStep {
        VerificationLinkStep to(EmailAddress to);
    }

    /**
     * Second required step: verification link.
     */
    public interface VerificationLinkStep {
        OptionalStep verificationLink(URI link);
    }

    /**
     * Optional fields for locale, id, createdAt, priority and userName.
     */
    public interface OptionalStep extends AbstractEmailJobBuilder.OptionalStep<OptionalStep, AccountVerificationEmailJob> {
        OptionalStep userName(String name);
    }

    /**
     * Terminal build step returning the immutable job.
     */
    public interface BuildStep extends AbstractEmailJobBuilder.BuildStep<AccountVerificationEmailJob> {
    }

    public static final class Builder extends AbstractEmailJobBuilder<Builder, AccountVerificationEmailJob>
            implements ToStep, VerificationLinkStep, OptionalStep, BuildStep {
        private EmailAddress to;
        private String userName;
        private URI verificationLink;

        private Builder() {
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public VerificationLinkStep to(EmailAddress to) {
            this.to = to;
            return this;
        }

        @Override
        public AccountVerificationEmailJob.OptionalStep verificationLink(URI link) {
            this.verificationLink = link;
            return this;
        }

        @Override
        public AccountVerificationEmailJob.OptionalStep userName(String name) {
            this.userName = name;
            return this;
        }

        @Override
        public AccountVerificationEmailJob build() {
            Map<String, Object> model = new HashMap<>();
            model.put("verificationLink", Objects.requireNonNull(verificationLink));
            if (userName != null) model.put("userName", userName);
            return new AccountVerificationEmailJob(id, to, locale, model, createdAt, priority);
        }
    }
}
