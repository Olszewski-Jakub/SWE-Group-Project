package ie.universityofgalway.groupnine.service.email.usecase;

import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.Priority;
import ie.universityofgalway.groupnine.domain.email.jobs.WelcomeEmailJob;
import ie.universityofgalway.groupnine.service.mailer.EmailMailerRegistry;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SendEmailUseCaseTest {
    @Test
    void delegatesToRegistry() {
        EmailMailerRegistry registry = mock(EmailMailerRegistry.class);
        SendEmailUseCase uc = new SendEmailUseCase(registry);
        var job = WelcomeEmailJob.builder()
                .to(new EmailAddress("to@example.com"))
                .id(EmailJobId.newId())
                .locale(Locale.ENGLISH)
                .priority(Priority.NORMAL)
                .build();
        uc.execute(job);
        verify(registry).process(any());
    }
}

