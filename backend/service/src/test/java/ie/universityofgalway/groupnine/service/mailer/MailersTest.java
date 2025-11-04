package ie.universityofgalway.groupnine.service.mailer;

import ie.universityofgalway.groupnine.domain.email.EmailAddress;
import ie.universityofgalway.groupnine.domain.email.EmailType;
import ie.universityofgalway.groupnine.domain.email.jobs.*;
import ie.universityofgalway.groupnine.service.email.port.IdempotencyPort;
import ie.universityofgalway.groupnine.service.email.port.RenderTemplatePort;
import ie.universityofgalway.groupnine.service.email.port.SendEmailPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MailersTest {

    private IdempotencyPort idem;
    private RenderTemplatePort renderer;
    private SendEmailPort sender;

    @BeforeEach
    void setup() {
        idem = Mockito.mock(IdempotencyPort.class);
        renderer = Mockito.mock(RenderTemplatePort.class);
        sender = Mockito.mock(SendEmailPort.class);
        when(idem.claim(any())).thenReturn(true);
        when(renderer.render(any(EmailType.class), any(), anyMap())).thenReturn(new RenderTemplatePort.RenderedEmail("Subj", "<h>html</h>", "txt"));
    }

    @Test
    void accountVerificationMailer_process_sends() {
        AccountVerificationMailer m = new AccountVerificationMailer(idem, renderer, sender, "from@example.com", "From");
        AccountVerificationEmailJob job = AccountVerificationEmailJob.builder()
                .to(new EmailAddress("u@example.com"))
                .verificationLink(URI.create("https://example.com/verify"))
                .build();

        m.process(job);
        verify(sender).send(eq("from@example.com"), eq("From"), eq("u@example.com"), eq("Subj"), anyString(), anyString());
    }

    @Test
    void passwordResetMailer_requires_resetLink_and_sends() {
        PasswordResetMailer m = new PasswordResetMailer(idem, renderer, sender, "from@example.com", "From");
        PasswordResetEmailJob job = PasswordResetEmailJob.builder()
                .to(new EmailAddress("u@example.com"))
                .resetLink(URI.create("https://example.com/reset"))
                .build();

        m.process(job);
        verify(sender).send(eq("from@example.com"), eq("From"), eq("u@example.com"), eq("Subj"), anyString(), anyString());
    }

    @Test
    void orderPaidMailer_processes() {
        OrderPaidMailer m = new OrderPaidMailer(idem, renderer, sender, "from@example.com", "From");
        OrderPaidEmailJob job = OrderPaidEmailJob.builder()
                .to(new EmailAddress("u@example.com"))
                .orderId("o1").amountMinor(123).currency("EUR")
                .build();
        m.process(job);
        verify(sender).send(eq("from@example.com"), eq("From"), eq("u@example.com"), eq("Subj"), anyString(), anyString());
    }

    @Test
    void orderRefundedMailer_processes() {
        OrderRefundedMailer m = new OrderRefundedMailer(idem, renderer, sender, "from@example.com", "From");
        OrderRefundedEmailJob job = OrderRefundedEmailJob.builder()
                .to(new EmailAddress("u@example.com"))
                .orderId("o1").amountMinor(123).currency("EUR")
                .build();
        m.process(job);
        verify(sender).send(eq("from@example.com"), eq("From"), eq("u@example.com"), eq("Subj"), anyString(), anyString());
    }

    @Test
    void paymentFailedMailer_processes_and_idempotency_false_skips() {
        PaymentFailedMailer m = new PaymentFailedMailer(idem, renderer, sender, "from@example.com", "From");
        PaymentFailedEmailJob job = PaymentFailedEmailJob.builder()
                .to(new EmailAddress("u@example.com"))
                .orderId("o1").reason("PAYMENT_FAILED")
                .build();

        // First: claimed -> send
        when(idem.claim(any())).thenReturn(true);
        m.process(job);
        verify(sender, times(1)).send(anyString(), anyString(), anyString(), anyString(), anyString(), any());

        // Second: not claimed -> skip
        reset(sender);
        when(idem.claim(any())).thenReturn(false);
        m.process(job);
        verifyNoInteractions(sender);
    }
}

