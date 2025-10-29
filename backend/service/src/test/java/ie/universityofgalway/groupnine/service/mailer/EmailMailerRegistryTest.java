package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.Priority;
import ie.universityofgalway.groupnine.domain.email.jobs.WelcomeEmailJob;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailMailerRegistryTest {
    @Test
    void dispatchesToRegisteredMailer() {
        BaseMailer<?> mailer = mock(BaseMailer.class);
        when(mailer.emailType()).thenReturn(ie.universityofgalway.groupnine.domain.email.EmailType.WELCOME);
        EmailMailerRegistry reg = new EmailMailerRegistry(List.of(mailer));
        var job = WelcomeEmailJob.builder().to(new EmailAddress("a@b.com")).id(EmailJobId.newId()).locale(Locale.ENGLISH).priority(Priority.NORMAL).build();
        reg.process(job);
        verify(mailer).processAny(job);
    }

    @Test
    void throwsWhenNoMailerRegistered() {
        EmailMailerRegistry reg = new EmailMailerRegistry(List.of());
        var job = WelcomeEmailJob.builder().to(new EmailAddress("a@b.com")).id(EmailJobId.newId()).locale(Locale.ENGLISH).priority(Priority.NORMAL).build();
        assertThrows(IllegalArgumentException.class, () -> reg.process(job));
    }
}

