package ie.universityofgalway.groupnine.delivery.worker.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import ie.universityofgalway.groupnine.domain.order.Order;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.order.OrderStatus;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import ie.universityofgalway.groupnine.service.messaging.port.ProcessedEventPort;
import ie.universityofgalway.groupnine.service.order.port.OrderPort;
import org.springframework.amqp.core.Message;
import io.micrometer.core.instrument.MeterRegistry;
import ie.universityofgalway.groupnine.util.logging.AppLogger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Consumes normalized {@code payment.event.*} messages and advances order state:
 * verifies amounts/currency, persists shipping details when available, transitions
 * orders to PAID/REFUNDED/PAYMENT_FAILED, coordinates inventory (confirm/release),
 * and publishes audit events. Idempotency is ensured per Stripe event id or message id.
 */
@Component
public class PaymentEventListener {

    private static final String QUEUE = "q.orders.payment-handler";

    private final ObjectMapper mapper;
    private final ProcessedEventPort processed;
    private final OrderPort orders;
    private final ShoppingCartPort carts;
    private final OutboxPort outbox;
    private final AuditEventPort audit;
    private final ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort emailEnqueue;
    private final ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort users;
    private final MeterRegistry metrics;
    private static final AppLogger LOG = AppLogger.get(PaymentEventListener.class);

    public PaymentEventListener(ObjectMapper mapper,
                                ProcessedEventPort processed,
                                OrderPort orders,
                                ShoppingCartPort carts,
                                OutboxPort outbox,
                                AuditEventPort audit,
                                ie.universityofgalway.groupnine.service.email.port.EnqueueEmailPort emailEnqueue,
                                ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort users,
                                MeterRegistry metrics) {
        this.mapper = mapper;
        this.processed = processed;
        this.orders = orders;
        this.carts = carts;
        this.outbox = outbox;
        this.audit = audit;
        this.emailEnqueue = emailEnqueue;
        this.users = users;
        this.metrics = metrics;
    }

    @RabbitListener(queues = QUEUE)
    public void onMessage(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            JsonNode root = mapper.readTree(message.getBody());
            String stripeEventId = asText(root, "stripe_event_id");
            String type = asText(root, "type");
            String orderIdStr = asText(root, "order_id");
            if (orderIdStr == null || orderIdStr.isBlank()) {
                // No order context; ignore
                channel.basicAck(tag, false);
                return;
            }

            String key = stripeEventId != null ? stripeEventId : (message.getMessageProperties().getMessageId());
            if (key != null && processed.alreadyProcessed("amqp:payments", key)) {
                channel.basicAck(tag, false);
                return;
            }

            OrderId orderId = new OrderId(java.util.UUID.fromString(orderIdStr));
            Optional<Order> maybe = orders.findById(orderId);
            if (maybe.isEmpty()) {
                channel.basicAck(tag, false);
                return;
            }

            Order order = maybe.get();

            // Always persist shipping info from checkout.session.completed (or any event carrying it),
            // even if order status has moved past PENDING_PAYMENT. This fixes missing shipping_amount/rate
            // when PaymentIntent events arrive before Session completed.
            if (type != null && type.contains("checkout.session.completed")) {
                Long shipMinor = root.hasNonNull("shipping_amount_minor") ? root.get("shipping_amount_minor").asLong() : null;
                String shipCur = asText(root, "shipping_currency");
                String shipRate = asText(root, "shipping_rate_id");
                String sname = asText(root, "shipping_name");
                String sphone = asText(root, "shipping_phone");
                String sline1 = asText(root, "shipping_address_line1");
                String sline2 = asText(root, "shipping_address_line2");
                String scity = asText(root, "shipping_city");
                String sstate = asText(root, "shipping_state");
                String spostal = asText(root, "shipping_postal_code");
                String scountry = asText(root, "shipping_country");
                if (shipMinor != null || shipCur != null || shipRate != null || sname != null || sphone != null || sline1 != null) {
                    order.setShipping(shipRate, shipMinor, shipCur, sname, sphone, sline1, sline2, scity, sstate, spostal, scountry);
                    orders.save(order);
                }
            }
            if (type != null && (type.contains("succeeded") || type.contains("completed"))) {
                if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                    // Verify amount/currency if present
                    Long amountMinor = root.hasNonNull("amount_minor") ? root.get("amount_minor").asLong() : null;
                    String currency = asText(root, "currency");
                    long expectedItemsMinor = toMinor(order.getTotal().getAmount(), order.getTotal().getCurrency());
                    Long shippingMinor = root.hasNonNull("shipping_amount_minor") ? root.get("shipping_amount_minor").asLong() : null;
                    Long persistedShippingMinor = order.getShippingAmountMinor();
                    long expectedShippingMinor = shippingMinor != null ? shippingMinor : (persistedShippingMinor != null ? persistedShippingMinor : 0L);
                    boolean haveShippingInfo = (shippingMinor != null) || (persistedShippingMinor != null);

                    boolean okAmount;
                    if (amountMinor == null) {
                        okAmount = true; // nothing to verify
                    } else if (haveShippingInfo) {
                        okAmount = (amountMinor == (expectedItemsMinor + expectedShippingMinor));
                    } else {
                        // Missing shipping in event and order; defer strict check to session.completed
                        okAmount = true;
                    }

                    boolean okCurrency = (currency == null) || order.getTotal().getCurrency().getCurrencyCode().equalsIgnoreCase(currency);
                    boolean ok = okAmount && okCurrency;
                    if (!ok) {
                        order.markPaymentFailed();
                        orders.save(order);
                        outbox.enqueue("inventory.commands", "inventory.release.request",
                                Map.of("order_id", order.getId().toString(), "reason", "VERIFICATION_FAILED"),
                                Map.of("order_id", order.getId().toString(), "reason", "VERIFICATION_FAILED"));
                        audit.record(null, "payment_verification_failed", Map.of(
                                "order_id", order.getId().toString(),
                                "expected_minor", expectedItemsMinor + expectedShippingMinor,
                                "got_minor", amountMinor,
                                "currency", currency
                        ), Instant.now());
                        if (metrics != null) metrics.counter("payment_verification_fail").increment();
                        LOG.warn("payment_verification_failed", "order_id", order.getId().toString(), "stripe_event_id", stripeEventId);
                        channel.basicAck(tag, false);
                        return;
                    }

                    // Persist shipping details if present
                    String sr = asText(root, "shipping_rate_id");
                    String scur = asText(root, "shipping_currency");
                    String sname = asText(root, "shipping_name");
                    String sphone = asText(root, "shipping_phone");
                    String sline1 = asText(root, "shipping_address_line1");
                    String sline2 = asText(root, "shipping_address_line2");
                    String scity = asText(root, "shipping_city");
                    String sstate = asText(root, "shipping_state");
                    String spostal = asText(root, "shipping_postal_code");
                    String scountry = asText(root, "shipping_country");
                    if (sr != null || shippingMinor != null || scur != null || sname != null || sphone != null) {
                        order.setShipping(sr, shippingMinor, scur, sname, sphone, sline1, sline2, scity, sstate, spostal, scountry);
                    }

                    order.markPaid();
                    orders.save(order);

                    // inventory.confirm.request (look up reservation by orderId downstream)
                    outbox.enqueue("inventory.commands", "inventory.confirm.request",
                            Map.of("order_id", order.getId().toString()),
                            Map.of("order_id", order.getId().toString()));

                    // lock cart
                    carts.findById(order.getCartId()).ifPresent(c -> { c.checkout(); carts.save(c); });

                    audit.record(null, "payment_verified", Map.of(
                            "order_id", order.getId().toString(),
                            "stripe_event_id", stripeEventId,
                            "amount_minor", amountMinor == null ? (expectedItemsMinor + expectedShippingMinor) : amountMinor,
                            "currency", currency == null ? order.getTotal().getCurrency().getCurrencyCode() : currency
                    ), Instant.now());
                    if (metrics != null) metrics.counter("payment_success").increment();
                    LOG.info("payment_succeeded", "order_id", order.getId().toString());

                    // send email
                    final long expectedTotalMinor = expectedItemsMinor + expectedShippingMinor;
                    users.findById(order.getUserId()).ifPresent(u -> {
                        var job = ie.universityofgalway.groupnine.domain.email.jobs.OrderPaidEmailJob.builder()
                                .to(new ie.universityofgalway.groupnine.domain.email.EmailAddress(u.getEmail().value()))
                                .orderId(order.getId().toString())
                                .amountMinor(expectedTotalMinor)
                                .currency(order.getTotal().getCurrency().getCurrencyCode())
                                .build();
                        emailEnqueue.enqueue(job);
                    });
                }
            } else if (type != null && type.contains("refunded")) {
                if (order.getStatus() == OrderStatus.PAID) {
                    order.markRefunded();
                    orders.save(order);
                    outbox.enqueue("inventory.commands", "inventory.release.request",
                            Map.of("order_id", order.getId().toString(), "reason", "REFUNDED"),
                            Map.of("order_id", order.getId().toString(), "reason", "REFUNDED"));
                    audit.record(null, "payment_refunded", Map.of("order_id", order.getId().toString()), Instant.now());
                    if (metrics != null) metrics.counter("payment_refund").increment();
                    LOG.info("payment_refunded", "order_id", order.getId().toString());
                    users.findById(order.getUserId()).ifPresent(u -> {
                        var job = ie.universityofgalway.groupnine.domain.email.jobs.OrderRefundedEmailJob.builder()
                                .to(new ie.universityofgalway.groupnine.domain.email.EmailAddress(u.getEmail().value()))
                                .orderId(order.getId().toString())
                                .amountMinor(toMinor(order.getTotal().getAmount(), order.getTotal().getCurrency()))
                                .currency(order.getTotal().getCurrency().getCurrencyCode())
                                .build();
                        emailEnqueue.enqueue(job);
                    });
                }
            } else if (type != null && type.contains("failed")) {
                if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                    order.markPaymentFailed();
                    orders.save(order);
                    outbox.enqueue("inventory.commands", "inventory.release.request",
                            Map.of("order_id", order.getId().toString(), "reason", "PAYMENT_FAILED"),
                            Map.of("order_id", order.getId().toString(), "reason", "PAYMENT_FAILED"));
                    audit.record(null, "payment_failed", Map.of("order_id", order.getId().toString()), Instant.now());
                    if (metrics != null) metrics.counter("payment_failed").increment();
                    LOG.warn("payment_failed", "order_id", order.getId().toString());
                    users.findById(order.getUserId()).ifPresent(u -> {
                        var job = ie.universityofgalway.groupnine.domain.email.jobs.PaymentFailedEmailJob.builder()
                                .to(new ie.universityofgalway.groupnine.domain.email.EmailAddress(u.getEmail().value()))
                                .orderId(order.getId().toString())
                                .reason("PAYMENT_FAILED")
                                .build();
                        emailEnqueue.enqueue(job);
                    });
                }
            }

            if (key != null) processed.markProcessed("amqp:payments", key);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            LOG.error("payment_event_handler_error", "error", e.getMessage());
            channel.basicReject(tag, false); // to DLQ
        }
    }

    private static String asText(JsonNode n, String field) {
        JsonNode f = n.get(field);
        return f == null || f.isNull() ? null : f.asText();
    }

    private static long toMinor(java.math.BigDecimal amount, java.util.Currency currency) {
        return amount.movePointRight(Math.max(currency.getDefaultFractionDigits(), 0)).longValueExact();
    }
}
