package ie.universityofgalway.groupnine.delivery.worker.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservation;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservationStatus;
import ie.universityofgalway.groupnine.domain.inventory.ReservationItem;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.inventory.port.InventoryAdjustmentPort;
import ie.universityofgalway.groupnine.service.inventory.port.InventoryReservationRepository;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import ie.universityofgalway.groupnine.service.messaging.port.ProcessedEventPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryReservationListenerTest {

    private ObjectMapper mapper;
    private ProcessedEventPort processed;
    private InventoryReservationRepository repo;
    private OutboxPort outbox;
    private AuditEventPort audit;
    private MeterRegistry metrics;
    private InventoryAdjustmentPort adjust;
    private InventoryReservationListener listener;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        processed = Mockito.mock(ProcessedEventPort.class);
        repo = Mockito.mock(InventoryReservationRepository.class);
        outbox = Mockito.mock(OutboxPort.class);
        audit = Mockito.mock(AuditEventPort.class);
        metrics = Mockito.mock(MeterRegistry.class, Mockito.RETURNS_DEEP_STUBS);
        adjust = Mockito.mock(InventoryAdjustmentPort.class);
        listener = new InventoryReservationListener(mapper, processed, repo, outbox, audit, metrics, adjust);
    }

    private Message jsonMsg(String json, String id) {
        MessageProperties p = new MessageProperties();
        p.setMessageId(id);
        p.setDeliveryTag(1L);
        return new Message(json.getBytes(), p);
    }

    @Test
    void reserve_success_persists_and_emits_event() throws Exception {
        when(processed.alreadyProcessed(anyString(), anyString())).thenReturn(false);
        when(adjust.tryReserve(any(VariantId.class), anyInt())).thenReturn(true);

        UUID orderUuid = UUID.randomUUID();
        String json = "{" +
                "\"order_id\":\"" + orderUuid + "\"," +
                "\"items\":[{\"variant_id\":\"" + UUID.randomUUID() + "\",\"quantity\":2}]," +
                "\"expires_at\":\"" + Instant.now().plusSeconds(60).toString() + "\"}";

        Channel ch = Mockito.mock(Channel.class);
        listener.onReserve(jsonMsg(json, "m1"), ch);

        ArgumentCaptor<InventoryReservation> cap = ArgumentCaptor.forClass(InventoryReservation.class);
        verify(repo).save(cap.capture());
        assertEquals(InventoryReservationStatus.RESERVED, cap.getValue().getStatus());
        verify(outbox).enqueue(eq("inventory.events"), eq("inventory.reserved"), anyMap(), anyMap());
        verify(ch).basicAck(1L, false);
        verify(processed).markProcessed(eq("amqp:inventory"), eq("m1"));
    }

    @Test
    void reserve_rejected_emits_rejected_event() throws Exception {
        when(processed.alreadyProcessed(anyString(), anyString())).thenReturn(false);
        when(adjust.tryReserve(any(VariantId.class), anyInt())).thenReturn(false);

        UUID orderUuid = UUID.randomUUID();
        String json = "{" +
                "\"order_id\":\"" + orderUuid + "\"," +
                "\"items\":[{\"variant_id\":\"" + UUID.randomUUID() + "\",\"quantity\":2}]," +
                "\"expires_at\":\"" + Instant.now().plusSeconds(60).toString() + "\"}";

        Channel ch = Mockito.mock(Channel.class);
        listener.onReserve(jsonMsg(json, "m2"), ch);

        verify(outbox).enqueue(eq("inventory.events"), eq("inventory.rejected"), anyMap(), anyMap());
        verify(ch).basicAck(1L, false);
    }

    @Test
    void confirm_moves_reserved_to_confirmed() throws Exception {
        OrderId oid = new OrderId(UUID.randomUUID());
        InventoryReservation res = InventoryReservation.pending(oid, List.of(new ReservationItem(new VariantId(UUID.randomUUID()), 1)), Instant.now());
        res.markReserved();
        when(repo.findByOrderId(eq(oid))).thenReturn(Optional.of(res));

        String json = "{\"order_id\":\"" + oid.toString() + "\"}";
        Channel ch = Mockito.mock(Channel.class);
        listener.onConfirm(jsonMsg(json, "m3"), ch);

        assertEquals(InventoryReservationStatus.CONFIRMED, res.getStatus());
        verify(repo).save(res);
        verify(outbox).enqueue(eq("inventory.events"), eq("inventory.confirmed"), anyMap(), anyMap());
        verify(ch).basicAck(1L, false);
    }

    @Test
    void release_moves_to_released_and_emits_reason() throws Exception {
        OrderId oid = new OrderId(UUID.randomUUID());
        InventoryReservation res = InventoryReservation.pending(oid, List.of(new ReservationItem(new VariantId(UUID.randomUUID()), 1)), Instant.now());
        when(repo.findByOrderId(eq(oid))).thenReturn(Optional.of(res));

        String json = "{\"order_id\":\"" + oid.toString() + "\",\"reason\":\"PAYMENT_FAILED\"}";
        Channel ch = Mockito.mock(Channel.class);
        listener.onRelease(jsonMsg(json, "m4"), ch);

        assertEquals(InventoryReservationStatus.RELEASED, res.getStatus());
        verify(repo).save(res);
        verify(outbox).enqueue(eq("inventory.events"), eq("inventory.released"), anyMap(), anyMap());
        verify(ch).basicAck(1L, false);
    }
}
