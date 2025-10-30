package ie.universityofgalway.groupnine.service.payments.webhook.usecase;

import ie.universityofgalway.groupnine.domain.payment.EventEnvelope;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import ie.universityofgalway.groupnine.service.messaging.port.ProcessedEventPort;
import ie.universityofgalway.groupnine.service.payments.webhook.port.StripeEventParserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProcessStripeWebhookUseCaseTest {

    private StripeEventParserPort parser;
    private ProcessedEventPort processed;
    private OutboxPort outbox;
    private AuditEventPort audit;

    @BeforeEach
    void setup() {
        parser = Mockito.mock(StripeEventParserPort.class);
        processed = Mockito.mock(ProcessedEventPort.class);
        outbox = Mockito.mock(OutboxPort.class);
        audit = Mockito.mock(AuditEventPort.class);
    }

    @Test
    void happy_path_enqueues_event_marks_processed_and_audits() throws Exception {
        EventEnvelope env = new EventEnvelope("evt_1", "checkout.session.completed", Instant.parse("2024-01-01T00:00:00Z"), Map.of("x", 1));
        when(parser.parse("p", "s")).thenReturn(env);
        when(processed.alreadyProcessed("stripe", "evt_1")).thenReturn(false);

        ProcessStripeWebhookUseCase uc = new ProcessStripeWebhookUseCase(parser, processed, outbox, audit);
        uc.execute("p", "s");

        ArgumentCaptor<Object> payloadCap = ArgumentCaptor.forClass(Object.class);
        verify(outbox).enqueue(eq("payments.events"), eq("payment.event.checkout.session.completed"), anyMap(), payloadCap.capture());
        assertTrue(payloadCap.getValue() instanceof Map);
        Map<?,?> sent = (Map<?,?>) payloadCap.getValue();
        assertEquals("evt_1", sent.get("stripe_event_id"));
        assertEquals("checkout.session.completed", sent.get("type"));

        verify(processed).markProcessed("stripe", "evt_1");
        verify(audit).record(isNull(), eq("webhook_received"), anyMap(), any());
    }

    @Test
    void already_processed_skips_enqueue_and_audit() throws Exception {
        EventEnvelope env = new EventEnvelope("evt_2", "any.type", Instant.now(), Map.of());
        when(parser.parse(anyString(), anyString())).thenReturn(env);
        when(processed.alreadyProcessed("stripe", "evt_2")).thenReturn(true);

        ProcessStripeWebhookUseCase uc = new ProcessStripeWebhookUseCase(parser, processed, outbox, audit);
        uc.execute("p", "s");

        verify(outbox, never()).enqueue(anyString(), anyString(), anyMap(), any());
        verify(processed, never()).markProcessed(anyString(), anyString());
        verifyNoInteractions(audit);
    }
}

