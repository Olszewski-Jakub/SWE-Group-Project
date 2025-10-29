package ie.universityofgalway.groupnine.infrastructure.email.adapter;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Thymeleaf-based renderer that resolves subject, HTML, and optional text bodies for emails.
 * <p>
 * Resolution strategy:
 * - Subject: message key "email.{type}.subject" from Spring {@link org.springframework.context.MessageSource},
 * with a fallback to classpath templates at templates/email/{type}/{locale}/subject.txt.
 * - HTML body: templates/email/{type}/{locale}/body.html rendered with the provided model.
 * - Text body: optional templates/email/{type}/{locale}/body.txt rendered as plain text.
 */
@Component
@Primary
public class ThymeleafTemplateRendererAdapter implements RenderTemplatePort {
    private final TemplateEngine engine;
    private final ResourceLoader resourceLoader;
    private final MessageSource messages;

    public ThymeleafTemplateRendererAdapter(TemplateEngine engine, ResourceLoader resourceLoader, MessageSource messages) {
        this.engine = engine;
        this.resourceLoader = resourceLoader;
        this.messages = messages;
    }

    @Override
    public RenderedEmail render(EmailType type, Locale locale, Map<String, Object> model) {
        Locale effective = locale != null ? locale : Locale.ENGLISH;
        Context ctx = new Context(effective, model);
        String typeKey = type.name().toLowerCase();

        // Resolve subject
        String subjectKey = "email." + typeKey + ".subject";
        String subject = messages.getMessage(subjectKey, null, null, effective);
        if (subject == null) {
            // fallback to templates/email/{type}/{locale}/subject.txt
            subject = readFirstAvailable(List.of(
                    "classpath:templates/email/" + typeKey + "/" + effective.toLanguageTag() + "/subject.txt",
                    "classpath:templates/email/" + typeKey + "/" + effective.getLanguage() + "/subject.txt",
                    "classpath:templates/email/" + typeKey + "/en/subject.txt"
            ));
            if (subject == null) subject = type.name();
        }

        // Resolve HTML body
        String htmlTemplate = firstExisting(List.of(
                "classpath:templates/email/" + typeKey + "/" + effective.toLanguageTag() + "/body.html",
                "classpath:templates/email/" + typeKey + "/" + effective.getLanguage() + "/body.html",
                "classpath:templates/email/" + typeKey + "/en/body.html"
        ));
        String html = htmlTemplate != null ? engine.process(htmlTemplate, ctx) : null;
        if (html == null) {
            // minimal fallback
            html = "<p>" + subject + "</p>";
        }

        // Resolve text body (optional)
        String text = readFirstAvailable(List.of(
                "classpath:templates/email/" + typeKey + "/" + effective.toLanguageTag() + "/body.txt",
                "classpath:templates/email/" + typeKey + "/" + effective.getLanguage() + "/body.txt",
                "classpath:templates/email/" + typeKey + "/en/body.txt"
        ));

        return new RenderedEmail(subject, html, text);
    }

    private String firstExisting(List<String> locations) {
        for (String loc : locations) {
            Resource r = resourceLoader.getResource(loc);
            if (r.exists()) {
                return toTemplateName(loc);
            }
        }
        return null;
    }

    private String toTemplateName(String classpathLoc) {
        String s = classpathLoc;
        if (s.startsWith("classpath:")) s = s.substring("classpath:".length());
        if (s.startsWith("/")) s = s.substring(1);
        if (s.startsWith("templates/")) s = s.substring("templates/".length());
        if (s.endsWith(".html")) s = s.substring(0, s.length() - 5);
        return s; // e.g., "email/account_verification/en/body"
    }

    private String readFirstAvailable(List<String> locations) {
        for (String loc : locations) {
            Resource r = resourceLoader.getResource(loc);
            if (r.exists()) {
                try (var in = r.getInputStream()) {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }
}
