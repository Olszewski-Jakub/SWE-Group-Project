package ie.universityofgalway.groupnine.delivery.worker.listener;

import com.rabbitmq.client.Channel;
import ie.universityofgalway.groupnine.domain.email.jobs.EmailJob;
import ie.universityofgalway.groupnine.service.email.serialization.EmailJobSerializer;
import ie.universityofgalway.groupnine.service.email.usecase.SendEmailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailJobListenerTest {

    private SendEmailUseCase sendEmail;
    private EmailJobSerializer serializer;
    private RabbitTemplate rabbit;
    private EmailJobListener listener;

    @BeforeEach
    void setup() {
        sendEmail = Mockito.mock(SendEmailUseCase.class);
        serializer = Mockito.mock(EmailJobSerializer.class);
        rabbit = Mockito.mock(RabbitTemplate.class);
        listener = new EmailJobListener(sendEmail, serializer, rabbit);
    }

    private Message msg(byte[] body, String consumerQueue, Integer attempt) {
        MessageProperties p = new MessageProperties();
        p.setConsumerQueue(consumerQueue);
        if (attempt != null) p.setHeader("x-attempt", attempt);
        Message m = new Message(body, p);
        p.setDeliveryTag(1L);
        return m;
    }

    @Test
    void success_acknowledges_message() throws Exception {
        var job = ie.universityofgalway.groupnine.domain.email.jobs.WelcomeEmailJob.builder()
                .to(new ie.universityofgalway.groupnine.domain.email.EmailAddress("user@example.com"))
                .build();
        when(serializer.fromBytes(any())).thenReturn(job);

        Channel ch = Mockito.mock(Channel.class);
        listener.onMessage(msg(new byte[]{1,2,3}, "email.welcome.q", 0), ch);

        verify(sendEmail).execute(job);
        verify(ch).basicAck(1L, false);
    }

    @Test
    void failure_publishes_retry_and_acks() throws Exception {
        when(serializer.fromBytes(any())).thenThrow(new RuntimeException("oops"));
        Channel ch = Mockito.mock(Channel.class);

        listener.onMessage(msg(new byte[]{1}, "email.account_verification.q", 0), ch);

        ArgumentCaptor<Message> retryCap = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq(""), eq("email.account_verification.q.retry.60000.q"), retryCap.capture());
        assertEquals(1, retryCap.getValue().getMessageProperties().getHeaders().get("x-attempt"));
        verify(ch).basicAck(1L, false);
    }

    @Test
    void max_attempts_rejects_to_dlq() throws Exception {
        when(serializer.fromBytes(any())).thenThrow(new RuntimeException("oops"));
        Channel ch = Mockito.mock(Channel.class);
        listener.onMessage(msg(new byte[]{1}, "email.password_reset.q", 6), ch);
        verify(ch).basicReject(1L, false);
        verifyNoInteractions(rabbit);
    }
}
