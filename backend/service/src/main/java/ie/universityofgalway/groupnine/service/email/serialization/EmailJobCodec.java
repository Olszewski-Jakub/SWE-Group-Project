package ie.universityofgalway.groupnine.service.email.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;

/**
 * Codec for serializing/deserializing the type-specific model of an {@link EmailJob}.
 * <p>
 * The common envelope fields (id, to, locale, priority, createdAt, type) are handled by the
 * {@link ie.universityofgalway.groupnine.infrastructure.email.serialization.CompositeEmailJobSerializer}.
 * Implementations focus on the model payload for a specific {@link EmailType}.
 */
public interface EmailJobCodec<T extends EmailJob> {
    /**
     * The {@link EmailType} this codec supports.
     */
    EmailType type();

    /**
     * Converts the job's template model into a JSON node for persistence on the queue.
     */
    JsonNode writeModel(T job);

    /**
     * Reconstructs a concrete {@link EmailJob} from its model node and envelope metadata.
     */
    T readModel(JsonNode modelNode, Envelope env);

    /**
     * Common envelope fields written alongside the model.
     * Values are represented as strings as they are originally JSON-serialized.
     */
    record Envelope(String id, String to, String locale, String priority, String createdAt) {
    }
}
