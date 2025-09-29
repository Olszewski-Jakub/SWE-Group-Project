package ie.universityofgalway.groupnine.service.email.serialization;

import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;

/**
 * Strategy for serializing/deserializing {@link EmailJob} messages exchanged with the queue.
 * Implementations may delegate to type-specific {@link EmailJobCodec} instances.
 */
public interface EmailJobSerializer {
    /**
     * Serializes an {@link EmailJob} into a compact byte array for transport.
     */
    byte[] toBytes(EmailJob job);

    /**
     * Deserializes bytes received from the queue into a concrete {@link EmailJob} instance.
     */
    EmailJob fromBytes(byte[] bytes);
}
