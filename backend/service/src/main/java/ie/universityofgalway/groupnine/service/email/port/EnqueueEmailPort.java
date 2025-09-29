package ie.universityofgalway.groupnine.service.email.port;

import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;

/**
 * Port for enqueuing email jobs to a message broker.
 * Implementations handle exchange, routing key, and message properties.
 */
public interface EnqueueEmailPort {
    /**
     * Enqueues a job for asynchronous processing by a worker.
     */
    void enqueue(EmailJob job);
}
