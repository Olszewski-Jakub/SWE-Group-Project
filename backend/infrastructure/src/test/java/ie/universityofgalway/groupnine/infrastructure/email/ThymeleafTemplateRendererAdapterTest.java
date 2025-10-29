package ie.universityofgalway.groupnine.infrastructure.email;

import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.infrastructure.email.adapter.ThymeleafTemplateRendererAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ThymeleafTemplateRendererAdapterTest {

    @Test
    void usesMessageSourceSubject_andTemplateHtml_andOptionalText() {
        TemplateEngine engine = mock(TemplateEngine.class);
        when(engine.process(any(String.class), any(Context.class))).thenReturn("<h1>Hello</h1>");

        ResourceLoader loader = mock(ResourceLoader.class);
        // text body available
        Resource textRes = new ByteArrayResource("Hello Text".getBytes(StandardCharsets.UTF_8));
        when(loader.getResource(contains("body.txt"))).thenReturn(textRes);
        // html template exists path
        Resource htmlRes = new ByteArrayResource(new byte[0]) {
            @Override public boolean exists() { return true; }
        };
        when(loader.getResource(contains("body.html"))).thenReturn(htmlRes);

        MessageSource messages = mock(MessageSource.class);
        when(messages.getMessage(eq("email.account_verification.subject"), any(), isNull(), any(Locale.class))).thenReturn("Verify your account");

        ThymeleafTemplateRendererAdapter adapter = new ThymeleafTemplateRendererAdapter(engine, loader, messages);
        var rendered = adapter.render(EmailType.ACCOUNT_VERIFICATION, Locale.ENGLISH, Map.of("verificationLink", "https://example.com"));
        assertEquals("Verify your account", rendered.subject());
        assertEquals("<h1>Hello</h1>", rendered.htmlBody());
        assertEquals("Hello Text", rendered.textBody());
    }

    @Test
    void fallsBackWhenNoMessageOrTemplate() {
        TemplateEngine engine = mock(TemplateEngine.class);
        ResourceLoader loader = mock(ResourceLoader.class);
        when(loader.getResource(any())).thenReturn(new ByteArrayResource(new byte[0]) { @Override public boolean exists(){ return false; }});
        MessageSource messages = mock(MessageSource.class);
        when(messages.getMessage(any(), any(), isNull(), any(Locale.class))).thenReturn(null);
        ThymeleafTemplateRendererAdapter adapter = new ThymeleafTemplateRendererAdapter(engine, loader, messages);
        var rendered = adapter.render(EmailType.WELCOME, Locale.ENGLISH, Map.of());
        assertEquals("WELCOME", rendered.subject());
        assertTrue(rendered.htmlBody().contains("<p>WELCOME</p>"));
        assertNull(rendered.textBody());
    }
}

