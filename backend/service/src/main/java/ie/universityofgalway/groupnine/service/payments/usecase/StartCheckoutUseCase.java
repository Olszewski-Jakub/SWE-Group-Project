package ie.universityofgalway.groupnine.service.payments.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.CartItem;
import ie.universityofgalway.groupnine.domain.cart.CartStatus;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.cart.exception.CartNotFoundException;
import ie.universityofgalway.groupnine.domain.order.Order;
import ie.universityofgalway.groupnine.domain.payment.IdempotencyKey;
import ie.universityofgalway.groupnine.domain.payment.OrderSnapshot;
import ie.universityofgalway.groupnine.domain.payment.OrderSnapshotItem;
import ie.universityofgalway.groupnine.domain.payment.PaymentMetadata;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import ie.universityofgalway.groupnine.service.order.port.OrderPort;
import ie.universityofgalway.groupnine.service.payments.dto.InventoryReserveRequestDto;
import ie.universityofgalway.groupnine.service.payments.dto.OrderCheckoutStartedDto;
import ie.universityofgalway.groupnine.service.payments.dto.StartCheckoutResultDto;
import ie.universityofgalway.groupnine.service.payments.port.PaymentGatewayPort;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutLineItem;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutSession;
import ie.universityofgalway.groupnine.service.payments.port.dto.ShippingOptions;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Orchestrates starting a Stripe Checkout from a cart, creating an Order (PENDING_PAYMENT),
 * creating a Stripe Checkout Session and enqueuing outbox messages for downstream processing.
 */
@Service
public class StartCheckoutUseCase {

    private final ShoppingCartPort carts;
    private final OrderPort orders;
    private final PaymentGatewayPort payments;
    private final OutboxPort outbox;
    private final AuditEventPort audit;
    private final ProductPort products;
    private final int reservationTtlSeconds;
    private final java.util.List<String> allowedCountries;
    private final java.util.List<String> shippingRateIds;

    @Autowired
    public StartCheckoutUseCase(
            ShoppingCartPort carts,
            OrderPort orders,
            PaymentGatewayPort payments,
            OutboxPort outbox,
            AuditEventPort audit,
            ProductPort products,
            @Value("${app.reservations.ttl-seconds:1800}") int reservationTtlSeconds,
            @Value("${app.checkout.shipping.allowed-countries:}") java.util.List<String> allowedCountries,
            @Value("${app.checkout.shipping.rate-ids:}") java.util.List<String> shippingRateIds
    ) {
        this.carts = Objects.requireNonNull(carts);
        this.orders = Objects.requireNonNull(orders);
        this.payments = Objects.requireNonNull(payments);
        this.outbox = Objects.requireNonNull(outbox);
        this.audit = Objects.requireNonNull(audit);
        this.products = Objects.requireNonNull(products);
        this.reservationTtlSeconds = reservationTtlSeconds;
        this.allowedCountries = allowedCountries == null ? java.util.List.of() : java.util.List.copyOf(allowedCountries);
        this.shippingRateIds = shippingRateIds == null ? java.util.List.of() : java.util.List.copyOf(shippingRateIds);
    }

    /**
     * Starts a checkout flow for the given cart and user.
     * <p>
     * Validates ownership and cart state, creates a pending {@code Order}, creates a hosted
     * checkout session via the configured gateway, transitions the cart to CHECKED_OUT,
     * publishes outbox messages for order/ inventory, and records an audit event.
     *
     * @param cartId     identifier of the shopping cart
     * @param userId     identifier of the authenticated user (must own the cart)
     * @param successUrl absolute URL the provider should redirect to on success
     * @param cancelUrl  absolute URL to return to if the customer cancels
     * @return a descriptor with order id, session id and hosted checkout URL
     * @throws ie.universityofgalway.groupnine.domain.cart.exception.CartNotFoundException when the cart does not exist
     * @throws IllegalArgumentException                                                    on ownership mismatch
     * @throws IllegalStateException                                                       when the cart is not ACTIVE or is empty
     */
    public StartCheckoutResultDto execute(CartId cartId, UserId userId, String successUrl, String cancelUrl) {
        ShoppingCart cart = carts.findById(cartId).orElseThrow(() -> new CartNotFoundException("Cart not found"));

        if (!cart.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cart ownership mismatch");
        }
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cart must be ACTIVE");
        }
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Money total = cart.total();
        Currency currency = total.getCurrency();

        Order order = Order.createPending(userId, cartId, total);
        order = orders.save(order);

        List<OrderSnapshotItem> items = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            String sku = ci.getVariant().getSku().getValue();
            long unitMinor = toMinor(ci.getVariant().getPrice().getAmount(), currency);
            items.add(new OrderSnapshotItem(ci.getVariant().getId(), sku, unitMinor, ci.getQuantity(), currency));
        }
        long totalMinor = toMinor(total.getAmount(), currency);
        OrderSnapshot snapshot = new OrderSnapshot(order.getId(), userId, cartId, items, totalMinor, currency);

        List<CheckoutLineItem> gatewayItems = new ArrayList<>();
        for (OrderSnapshotItem it : items) {
            String displayName = products.findByVariantId(it.getVariantId())
                    .map(Product::getName)
                    .orElse(it.getSku());
            gatewayItems.add(new CheckoutLineItem(displayName, it.getUnitAmountMinor(), it.getQuantity()));
        }

        PaymentMetadata paymentMetadata = PaymentMetadata.builder().orderId(order.getId()).cartId(cartId).userId(userId).build();

        IdempotencyKey idempotencyKey = IdempotencyKey.from(order.getId(), snapshot);

        CheckoutSession session = payments.createCheckoutSession(
                gatewayItems,
                currency.getCurrencyCode(),
                paymentMetadata,
                successUrl,
                cancelUrl,
                idempotencyKey,
                new ShippingOptions(allowedCountries, shippingRateIds)
        );

        cart.checkout();
        carts.save(cart);

        Map<String, Object> headers = Map.of(
                "correlation_id", order.getId().toString(),
                "order_id", order.getId().toString()
        );

        OrderCheckoutStartedDto evt = new OrderCheckoutStartedDto(
                order.getId().toString(),
                session.getSessionId(),
                session.getPaymentIntentId(),
                totalMinor,
                currency.getCurrencyCode()
        );

        outbox.enqueue("orders.commands", "order.checkout.started", headers, evt);

        InventoryReserveRequestDto reserve = new InventoryReserveRequestDto(
                order.getId().toString(),
                items,
                Instant.now().plusSeconds(reservationTtlSeconds).toString()
        );
        outbox.enqueue("inventory.commands", "inventory.reserve.request", headers, reserve);

        audit.record(userId, "checkout_started", Map.of(
                "order_id", order.getId().toString(),
                "cart_id", cartId.toString(),
                "amount_minor", totalMinor,
                "currency", currency.getCurrencyCode()
        ), Instant.now());

        return new StartCheckoutResultDto(order.getId(), session.getSessionId(), session.getUrl());
    }

    /**
     * Converts a decimal amount to minor currency units using the currency's default
     * fraction digits. For example, EUR 12.34 becomes {@code 1234}.
     */
    private static long toMinor(BigDecimal amount, Currency currency) {
        int fraction = currency.getDefaultFractionDigits();
        return amount.movePointRight(Math.max(fraction, 0)).longValueExact();
    }
}
