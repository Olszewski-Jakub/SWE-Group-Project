package ie.universityofgalway.groupnine.infrastructure.email;

import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailJobId;
import ie.universityofgalway.groupnine.domain.email.Priority;
import ie.universityofgalway.groupnine.domain.email.jobs.AccountVerificationEmailJob;
import ie.universityofgalway.groupnine.infrastructure.email.adapter.RabbitEmailPublisherAdapter;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.net.URI;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RabbitEmailPublisherAdapterTest {

    @Test
    void sendsSerializedMessageToExchangeWithRoutingKey() {
        RabbitTemplate rt = mock(RabbitTemplate.class);
        EmailJobSerializer serializer = mock(EmailJobSerializer.class);
        when(serializer.toBytes(any())).thenReturn("{}".getBytes());
        RabbitEmailPublisherAdapter adapter = new RabbitEmailPublisherAdapter(rt, serializer);

        AccountVerificationEmailJob job = AccountVerificationEmailJob.builder()
                .to(new EmailAddress("to@example.com"))
                .verificationLink(URI.create("https://x"))
                .id(EmailJobId.newId())
                .locale(Locale.ENGLISH)
                .priority(Priority.NORMAL)
                .build();

        adapter.enqueue(job);
        verify(rt).send(eq("email.exchange"), eq("email.account_verification"), any(Message.class));
    }
}

