package ie.universityofgalway.groupnine.service.email.port;

import ie.universityofgalway.groupnine.domain.email.EmailType;

import java.util.Locale;
import java.util.Map;

/**
 * Port to render email templates into a subject, HTML body, and optional text body for a given email type.
 */
public interface RenderTemplatePort {
    /**
     * Renders the email payload for the given type and locale using the provided template model.
     */
    RenderedEmail render(EmailType type, Locale locale, Map<String, Object> model);

    /**
     * DTO for rendered email parts.
     */
    record RenderedEmail(String subject, String htmlBody, String textBody) {
    }
}
