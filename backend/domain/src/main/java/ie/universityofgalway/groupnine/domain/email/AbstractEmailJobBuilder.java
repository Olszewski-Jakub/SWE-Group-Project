package ie.universityofgalway.groupnine.domain.email;

import java.time.Instant;
import java.util.Locale;

/**
 * Base builder providing common optional fields across all email jobs and fluent setters.
 * Concrete job builders extend this class and provide their own required steps and build logic.
 */
public abstract class AbstractEmailJobBuilder<SELF extends AbstractEmailJobBuilder<SELF, BUILT>, BUILT> {
    protected EmailJobId id = EmailJobId.newId();
    protected Locale locale = Locale.ENGLISH;
    protected Priority priority = Priority.NORMAL;
    protected Instant createdAt = Instant.now();

    protected abstract SELF self();

    // Common optional fluent setters (inherited by concrete builders)
    public SELF id(EmailJobId id) {
        this.id = id;
        return self();
    }

    public SELF locale(Locale locale) {
        this.locale = locale;
        return self();
    }

    public SELF priority(Priority priority) {
        this.priority = priority;
        return self();
    }

    public SELF createdAt(Instant instant) {
        this.createdAt = instant;
        return self();
    }

    /**
     * Concrete builders must implement build().
     */
    public abstract BUILT build();

    /**
     * Terminal build step.
     */
    public interface BuildStep<B> {
        B build();
    }

    /**
     * Optional step common to all email jobs for setting meta fields like id, locale, priority, and createdAt.
     */
    public interface OptionalStep<S extends OptionalStep<S, B>, B> extends BuildStep<B> {
        S id(EmailJobId id);

        S locale(Locale locale);

        S priority(Priority priority);

        S createdAt(Instant instant);
    }
}
