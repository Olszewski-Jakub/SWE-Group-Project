package ie.universityofgalway.groupnine.delivery.worker.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.order.Order;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.order.OrderStatus;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import ie.universityofgalway.groupnine.service.messaging.port.ProcessedEventPort;
import ie.universityofgalway.groupnine.service.order.port.OrderPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentEventListenerTest {

    private ObjectMapper mapper;
    private ProcessedEventPort processed;
    private OrderPort orders;
    private ShoppingCartPort carts;
    private OutboxPort outbox;
    private AuditEventPort audit;
    private ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort email;
    private ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort users;
    private MeterRegistry metrics;
    private PaymentEventListener listener;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        processed = Mockito.mock(ProcessedEventPort.class);
        orders = Mockito.mock(OrderPort.class);
        carts = Mockito.mock(ShoppingCartPort.class);
        outbox = Mockito.mock(OutboxPort.class);
        audit = Mockito.mock(AuditEventPort.class);
        email = Mockito.mock(ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort.class);
        users = Mockito.mock(ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort.class);
        metrics = Mockito.mock(MeterRegistry.class, Mockito.RETURNS_DEEP_STUBS);
        listener = new PaymentEventListener(mapper, processed, orders, carts, outbox, audit, email, users, metrics);
    }

    private Order pendingOrder() {
        return new Order(
                OrderId.of(UUID.randomUUID()),
                UserId.of(UUID.randomUUID()),
                CartId.of(UUID.randomUUID()),
                new Money(new BigDecimal("12.34"), Currency.getInstance("EUR")),
                OrderStatus.PENDING_PAYMENT,
                java.time.Instant.now(),
                java.time.Instant.now()
        );
    }

    private Message jsonMsg(String json, String msgId) {
        MessageProperties p = new MessageProperties();
        p.setDeliveryTag(1L);
        p.setMessageId(msgId);
        return new Message(json.getBytes(), p);
    }

    @Test
    void refunded_from_paid_releases_inventory() throws Exception {
        Order order = pendingOrder();
        order.markPaid();
        when(orders.findById(eq(order.getId()))).thenReturn(Optional.of(order));

        String json = "{\"type\":\"charge.refunded\",\"order_id\":\"" + order.getId().toString() + "\"}";
        Channel ch = Mockito.mock(Channel.class);
        listener.onMessage(jsonMsg(json, "e2"), ch);

        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        verify(outbox).enqueue(eq("inventory.commands"), eq("inventory.release.request"), anyMap(), anyMap());
        verify(ch).basicAck(1L, false);
    }

    @Test
    void failed_from_pending_marks_failed_and_releases_inventory() throws Exception {
        Order order = pendingOrder();
        when(orders.findById(eq(order.getId()))).thenReturn(Optional.of(order));

        String json = "{\"type\":\"payment_intent.payment_failed\",\"order_id\":\"" + order.getId().toString() + "\"}";
        Channel ch = Mockito.mock(Channel.class);
        listener.onMessage(jsonMsg(json, "e3"), ch);

        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        verify(outbox).enqueue(eq("inventory.commands"), eq("inventory.release.request"), anyMap(), anyMap());
        verify(ch).basicAck(1L, false);
    }
}
