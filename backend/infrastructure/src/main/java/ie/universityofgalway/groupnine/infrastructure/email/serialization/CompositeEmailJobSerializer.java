package ie.universityofgalway.groupnine.infrastructure.email.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobCodec;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobSerializer;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Composite serializer that handles the common envelope fields for {@link EmailJob} and delegates
 * type-specific model (payload) serialization to registered {@link EmailJobCodec} implementations.
 */
@Component
public class CompositeEmailJobSerializer implements EmailJobSerializer {
    private static final int SCHEMA_VERSION = 1;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<EmailType, EmailJobCodec<?>> codecs;

    public CompositeEmailJobSerializer(List<EmailJobCodec<?>> codecs) {
        EnumMap<EmailType, EmailJobCodec<?>> map = new EnumMap<>(EmailType.class);
        for (EmailJobCodec<?> c : codecs) map.put(c.type(), c);
        this.codecs = Map.copyOf(map);
    }

    @Override
    public byte[] toBytes(EmailJob job) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("schemaVersion", SCHEMA_VERSION);
            root.put("type", job.type().name());
            root.put("id", job.id().value().toString());
            root.put("to", job.to().value());
            root.put("locale", job.locale().toLanguageTag());
            root.put("priority", job.priority().name());
            root.put("createdAt", job.createdAt().toString());
            @SuppressWarnings("unchecked")
            EmailJobCodec<EmailJob> codec = (EmailJobCodec<EmailJob>) codecs.get(job.type());
            if (codec == null) throw new IllegalArgumentException("No codec for type " + job.type());
            JsonNode model = codec.writeModel(job);
            root.set("model", model);
            return mapper.writeValueAsBytes(root);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Serialize EmailJob failed", e);
        }
    }

    @Override
    public EmailJob fromBytes(byte[] bytes) {
        try {
            JsonNode n = mapper.readTree(bytes);
            String type = n.path("type").asText();
            int schema = n.path("schemaVersion").asInt(1);
            if (schema != 1) throw new IllegalArgumentException("Unsupported schema version: " + schema);
            EmailType t = EmailType.valueOf(type);
            EmailJobCodec.Envelope env = new EmailJobCodec.Envelope(
                    n.path("id").asText(),
                    n.path("to").asText(),
                    n.path("locale").asText("en"),
                    n.path("priority").asText("NORMAL"),
                    n.path("createdAt").asText()
            );
            @SuppressWarnings("unchecked")
            EmailJobCodec<EmailJob> codec = (EmailJobCodec<EmailJob>) codecs.get(t);
            if (codec == null) throw new IllegalArgumentException("No codec for type " + t);
            return codec.readModel(n.path("model"), env);
        } catch (Exception e) {
            throw new IllegalArgumentException("Deserialize EmailJob failed", e);
        }
    }
}
