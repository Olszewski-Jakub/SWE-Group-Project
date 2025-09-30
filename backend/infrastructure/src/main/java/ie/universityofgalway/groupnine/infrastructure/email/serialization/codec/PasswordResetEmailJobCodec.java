package ie.universityofgalway.groupnine.infrastructure.email.serialization.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.Priority;
import ie.universityofgalway.groupnine.domain.email.jobs.PasswordResetEmailJob;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobCodec;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Component
public class PasswordResetEmailJobCodec implements EmailJobCodec<PasswordResetEmailJob> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public EmailType type() {
        return EmailType.PASSWORD_RESET;
    }

    @Override
    public JsonNode writeModel(PasswordResetEmailJob job) {
        ObjectNode model = mapper.createObjectNode();
        model.put("resetLink", String.valueOf(job.templateModel().get("resetLink")));
        if (job.templateModel().get("userName") != null) {
            model.put("userName", String.valueOf(job.templateModel().get("userName")));
        }
        return model;
    }

    @Override
    public PasswordResetEmailJob readModel(JsonNode modelNode, Envelope env) {
        EmailJobId id = new EmailJobId(UUID.fromString(env.id()));
        EmailAddress to = new EmailAddress(env.to());
        Locale locale = Locale.forLanguageTag(env.locale());
        Priority pr = Priority.valueOf(env.priority());
        Instant createdAt = Instant.parse(env.createdAt());
        URI link = URI.create(modelNode.path("resetLink").asText());
        PasswordResetEmailJob.OptionalStep opt = PasswordResetEmailJob.builder()
                .to(to)
                .resetLink(link)
                .id(id)
                .locale(locale)
                .priority(pr)
                .createdAt(createdAt);
        if (modelNode.hasNonNull("userName")) {
            opt = opt.userName(modelNode.path("userName").asText());
        }
        return opt.build();
    }
}

