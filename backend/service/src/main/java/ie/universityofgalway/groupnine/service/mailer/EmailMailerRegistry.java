package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable registry mapping {@link EmailType} to a concrete {@link BaseMailer}.
 * Constructed by Spring with the discovered mailers, enabling O(1) dispatch.
 */
public class EmailMailerRegistry {
    private final Map<EmailType, BaseMailer<?>> mailers;

    public EmailMailerRegistry(List<BaseMailer<?>> mailers) {
        EnumMap<EmailType, BaseMailer<?>> map = new EnumMap<>(EmailType.class);
        for (BaseMailer<?> m : mailers) map.put(m.emailType(), m);
        this.mailers = Map.copyOf(map);
    }

    /**
     * Dispatches the given job to its configured {@link BaseMailer}.
     *
     * @throws IllegalArgumentException if no mailer is registered for the job type
     */
    public void process(EmailJob job) {
        BaseMailer<?> m = mailers.get(job.type());
        if (m == null) throw new IllegalArgumentException("No mailer for type " + job.type());
        m.processAny(job);
    }
}
