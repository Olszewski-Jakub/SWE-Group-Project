package ie.universityofgalway.groupnine.service.email.usecase;

import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;
import ie.universityofgalway.groupnine.service.mailer.EmailMailerRegistry;

/**
 * Application use case that routes a deserialized {@link EmailJob} to the
 * appropriate {@link ie.universityofgalway.groupnine.service.mailer.BaseMailer} via
 * the {@link EmailMailerRegistry}. It contains no transport or rendering logic.
 */
public class SendEmailUseCase {
    private final EmailMailerRegistry registry;

    public SendEmailUseCase(EmailMailerRegistry registry) {
        this.registry = registry;
    }

    public void execute(EmailJob job) {
        registry.process(job);
    }
}
