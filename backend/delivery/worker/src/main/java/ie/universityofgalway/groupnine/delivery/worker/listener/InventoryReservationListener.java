package ie.universityofgalway.groupnine.delivery.worker.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservation;
import ie.universityofgalway.groupnine.domain.inventory.InventoryReservationStatus;
import ie.universityofgalway.groupnine.domain.inventory.ReservationItem;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.inventory.port.InventoryReservationRepository;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import ie.universityofgalway.groupnine.service.messaging.port.ProcessedEventPort;
import ie.universityofgalway.groupnine.service.inventory.port.InventoryAdjustmentPort;
import org.springframework.amqp.core.Message;
import io.micrometer.core.instrument.MeterRegistry;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * Handles inventory reservation commands (reserve, confirm, release) consumed from AMQP queues.
 * The listener persists reservation state, adjusts stock via the inventory port and emits
 * normalized events to the outbox for further processing. Idempotency keys are derived from
 * message identifiers and tracked via {@code ProcessedEventPort}.
 */
@Component
public class InventoryReservationListener {

    private final ObjectMapper mapper;
    private final ProcessedEventPort processed;
    private final InventoryReservationRepository reservations;
    private final OutboxPort outbox;
    private final AuditEventPort audit;
    private final MeterRegistry metrics;
    private final InventoryAdjustmentPort inventoryAdjust;
    private static final AppLogger LOG = AppLogger.get(InventoryReservationListener.class);

    public InventoryReservationListener(ObjectMapper mapper,
                                        ProcessedEventPort processed,
                                        InventoryReservationRepository reservations,
                                        OutboxPort outbox,
                                        AuditEventPort audit,
                                        MeterRegistry metrics,
                                        InventoryAdjustmentPort inventoryAdjust) {
        this.mapper = mapper;
        this.processed = processed;
        this.reservations = reservations;
        this.outbox = outbox;
        this.audit = audit;
        this.metrics = metrics;
        this.inventoryAdjust = inventoryAdjust;
    }

    @RabbitListener(queues = "q.inventory.reservation")
    @Transactional
    public void onReserve(Message message, Channel channel) throws IOException {
        handle(message, channel, Action.RESERVE);
    }

    @RabbitListener(queues = "q.inventory.confirm")
    @Transactional
    public void onConfirm(Message message, Channel channel) throws IOException {
        handle(message, channel, Action.CONFIRM);
    }

    @RabbitListener(queues = "q.inventory.release")
    public void onRelease(Message message, Channel channel) throws IOException {
        handle(message, channel, Action.RELEASE);
    }

    private void handle(Message message, Channel channel, Action action) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String msgId = Optional.ofNullable(message.getMessageProperties().getMessageId()).orElse(UUID.randomUUID().toString());
        try {
            if (processed.alreadyProcessed("amqp:inventory", msgId)) {
                channel.basicAck(tag, false);
                return;
            }
            JsonNode root = mapper.readTree(message.getBody());
            String orderIdStr = asText(root, "order_id");
            if (orderIdStr == null) { channel.basicAck(tag, false); return; }
            OrderId orderId = new OrderId(UUID.fromString(orderIdStr));

            switch (action) {
                case RESERVE -> doReserve(root, orderId);
                case CONFIRM -> doConfirm(orderId);
                case RELEASE -> doRelease(orderId, asText(root, "reason"));
            }

            processed.markProcessed("amqp:inventory", msgId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicReject(tag, false);
        }
    }

    private void doReserve(JsonNode root, OrderId orderId) {
        List<ReservationItem> items = new ArrayList<>();
        JsonNode arr = root.get("items");
        if (arr != null && arr.isArray()) {
            for (JsonNode it : arr) {
                String variantId = asText(it, "variant_id");
                int quantity = it.get("quantity").asInt(0);
                if (variantId != null && quantity > 0) {
                    items.add(new ReservationItem(new ie.universityofgalway.groupnine.domain.product.VariantId(UUID.fromString(variantId)), quantity));
                }
            }
        }
        Instant expiresAt = Instant.parse(asText(root, "expires_at"));
        InventoryReservation ir = InventoryReservation.pending(orderId, items, expiresAt);
        // Attempt atomic reservation per item; rollback if any fail
        List<ReservationItem> failed = new ArrayList<>();
        for (ReservationItem it : items) {
            boolean ok = inventoryAdjust.tryReserve(it.getVariantId(), it.getQuantity());
            if (!ok) failed.add(it);
        }
        if (failed.isEmpty()) {
            ir.markReserved();
            reservations.save(ir);
            outbox.enqueue("inventory.events", "inventory.reserved", Map.of("order_id", orderId.toString()), Map.of("order_id", orderId.toString()));
            audit.record(null, "inventory_reserved", Map.of("order_id", orderId.toString()), Instant.now());
            if (metrics != null) metrics.counter("reserve_success").increment();
            LOG.info("inventory_reserved", "order_id", orderId.toString());
        } else {
            // Publish rejection with details; do not persist reservation as RESERVED
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("order_id", orderId.toString());
            List<Map<String, Object>> fItems = new ArrayList<>();
            for (ReservationItem fi : failed) {
                fItems.add(Map.of("variant_id", fi.getVariantId().getId().toString(), "requested", fi.getQuantity()));
            }
            payload.put("rejected_items", fItems);
            outbox.enqueue("inventory.events", "inventory.rejected", Map.of("order_id", orderId.toString()), payload);
            audit.record(null, "inventory_rejected", Map.of("order_id", orderId.toString(), "count", failed.size()), Instant.now());
            if (metrics != null) metrics.counter("reserve_reject").increment();
            LOG.warn("inventory_rejected", "order_id", orderId.toString(), "count", failed.size());
        }
    }

    private void doConfirm(OrderId orderId) {
        reservations.findByOrderId(orderId).ifPresent(ir -> {
            if (ir.getStatus() == InventoryReservationStatus.RESERVED) {
                for (ReservationItem it : ir.getItems()) {
                    inventoryAdjust.decrementReserved(it.getVariantId(), it.getQuantity());
                    inventoryAdjust.decrementTotalStock(it.getVariantId(), it.getQuantity());
                }
                ir.confirm();
                reservations.save(ir);
                outbox.enqueue("inventory.events", "inventory.confirmed", Map.of("order_id", orderId.toString()), Map.of("order_id", orderId.toString()));
                audit.record(null, "inventory_confirmed", Map.of("order_id", orderId.toString()), Instant.now());
                if (metrics != null) metrics.counter("reserve_confirm").increment();
                LOG.info("inventory_confirmed", "order_id", orderId.toString());
            }
        });
    }

    private void doRelease(OrderId orderId, String reason) {
        reservations.findByOrderId(orderId).ifPresent(ir -> {
            if (ir.getStatus() == InventoryReservationStatus.RESERVED || ir.getStatus() == InventoryReservationStatus.PENDING) {
                // Decrement reserved counters only; total stock unchanged
                for (ReservationItem it : ir.getItems()) inventoryAdjust.decrementReserved(it.getVariantId(), it.getQuantity());
                ir.release();
                reservations.save(ir);
                outbox.enqueue("inventory.events", "inventory.released", Map.of("order_id", orderId.toString(), "reason", reason), Map.of("order_id", orderId.toString(), "reason", reason));
                audit.record(null, "inventory_released", Map.of("order_id", orderId.toString(), "reason", reason), Instant.now());
                if (metrics != null) metrics.counter("reserve_release").increment();
                LOG.info("inventory_released", "order_id", orderId.toString(), "reason", reason);
            }
        });
    }

    private static String asText(JsonNode n, String field) {
        JsonNode f = n.get(field);
        return f == null || f.isNull() ? null : f.asText();
    }

    private enum Action { RESERVE, CONFIRM, RELEASE }
}
