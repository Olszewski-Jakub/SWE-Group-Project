package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort.RenderedEmail;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;
import ie.universityofgalway.groupnine.util.logging.AppLogger;

import java.util.Objects;

/**
 * Base class encapsulating the common flow for sending a particular type of email:
 * idempotency guard, template rendering, and transport via {@link ie.universityofgalway.groupnine.service.email.port.SendEmailPort}.
 * Subclasses validate their domain-specific model and define the {@link EmailType} they handle.
 */
public abstract class BaseMailer<T extends EmailJob> {
    protected final IdempotencyPort idempotency;
    protected final RenderTemplatePort renderer;
    protected final SendEmailPort sender;
    protected final String fromEmail;
    protected final String fromName;
    protected final AppLogger log;

    protected BaseMailer(IdempotencyPort idempotency,
                         RenderTemplatePort renderer,
                         SendEmailPort sender,
                         String fromEmail,
                         String fromName,
                         Class<?> loggerType) {
        this.idempotency = Objects.requireNonNull(idempotency);
        this.renderer = Objects.requireNonNull(renderer);
        this.sender = Objects.requireNonNull(sender);
        this.fromEmail = Objects.requireNonNull(fromEmail);
        this.fromName = Objects.requireNonNull(fromName);
        this.log = AppLogger.get(loggerType);
    }

    /**
     * Validates and sends the email if it hasn't been processed yet.
     */
    public final void process(T job) {
        validate(job);
        if (!idempotency.claim(job.id())) {
            recordSkip(job);
            return;
        }
        RenderedEmail email = renderer.render(job.type(), job.locale(), job.templateModel());
        beforeSend(job, email);
        sender.send(fromEmail, fromName, job.to().value(), email.subject(), email.htmlBody(), email.textBody());
        afterSend(job, email);
    }

    @SuppressWarnings("unchecked")
    /**
     * Bridges erased generic type to allow registry-based dispatching.
     */
    public final void processAny(EmailJob job) {
        process((T) job);
    }

    /**
     * The {@link EmailType} this mailer supports.
     */
    public abstract EmailType emailType();

    /**
     * Validates domain-specific model requirements.
     */
    protected abstract void validate(T job);

    /**
     * Hook executed after rendering but before transport.
     */
    protected void beforeSend(T job, RenderedEmail email) {
    }

    /**
     * Hook executed after successful transport.
     */
    protected void afterSend(T job, RenderedEmail email) {
        log.info("email_sent", "type", job.type().name(), "to", job.to().value());
    }

    /**
     * Records a skip when idempotency check fails (duplicate).
     */
    protected void recordSkip(T job) {
        log.info("email_skipped_idempotent", "type", job.type().name(), "id", job.id().value().toString());
    }
}
