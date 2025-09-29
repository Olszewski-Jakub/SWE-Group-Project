package ie.universityofgalway.groupnine.service.email.port;

import ie.universityofgalway.groupnine.domain.email.EmailJobId;

/**
 * Port to deduplicate email processing based on a unique job id (at-least-once semantics).
 */
public interface IdempotencyPort {
    /**
     * Attempts to claim the job id for processing.
     *
     * @return true if the id was previously unseen and is now marked as processed, false if it was already claimed
     */
    boolean claim(EmailJobId id);
}
