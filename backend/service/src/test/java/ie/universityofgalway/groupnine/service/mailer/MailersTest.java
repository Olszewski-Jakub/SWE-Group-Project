package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.Priority;
import ie.universityofgalway.groupnine.domain.email.jobs.AccountVerificationEmailJob;
import ie.universityofgalway.groupnine.domain.email.jobs.PasswordResetEmailJob;
import ie.universityofgalway.groupnine.domain.email.jobs.WelcomeEmailJob;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MailersTest {
    private RenderTemplatePort.RenderedEmail okEmail() { return new RenderTemplatePort.RenderedEmail("s","<p>h</p>","t"); }

    @Test
    void welcomeMailer_sendsWhenIdempotent() {
        IdempotencyPort idem = id -> true;
        RenderTemplatePort renderer = (type, locale, model) -> okEmail();
        SendEmailPort sender = mock(SendEmailPort.class);
        WelcomeMailer m = new WelcomeMailer(idem, renderer, sender, "noreply@example.com", "Example");
        var job = WelcomeEmailJob.builder().to(new EmailAddress("to@example.com")).id(EmailJobId.newId()).locale(Locale.ENGLISH).priority(Priority.NORMAL).build();
        m.process(job);
        verify(sender).send(any(), any(), eq("to@example.com"), any(), any(), any());
    }
}
