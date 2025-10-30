package ie.universityofgalway.groupnine.domain.order;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Order aggregate capturing checkout intent and payment lifecycle.
 */
public final class Order {
    private final OrderId id;
    private final UserId userId;
    private final CartId cartId;
    private final Money total;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    // Shipping & delivery
    private String shippingRateId;
    private Long shippingAmountMinor;
    private String shippingCurrency;
    private String shippingName;
    private String shippingPhone;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    public Order(OrderId id, UserId userId, CartId cartId, Money total, OrderStatus status,
                 Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.cartId = Objects.requireNonNull(cartId, "cartId");
        this.total = Objects.requireNonNull(total, "total");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Order createPending(UserId userId, CartId cartId, Money total) {
        Instant now = Instant.now();
        return new Order(OrderId.newId(), userId, cartId, total, OrderStatus.PENDING_PAYMENT, now, now);
    }

    public OrderId getId() { return id; }
    public UserId getUserId() { return userId; }
    public CartId getCartId() { return cartId; }
    public Money getTotal() { return total; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Shipping getters
    public String getShippingRateId() { return shippingRateId; }
    public Long getShippingAmountMinor() { return shippingAmountMinor; }
    public String getShippingCurrency() { return shippingCurrency; }
    public String getShippingName() { return shippingName; }
    public String getShippingPhone() { return shippingPhone; }
    public String getShippingAddressLine1() { return shippingAddressLine1; }
    public String getShippingAddressLine2() { return shippingAddressLine2; }
    public String getShippingCity() { return shippingCity; }
    public String getShippingState() { return shippingState; }
    public String getShippingPostalCode() { return shippingPostalCode; }
    public String getShippingCountry() { return shippingCountry; }

    public void markPaid() {
        ensureState(OrderStatus.PENDING_PAYMENT);
        this.status = OrderStatus.PAID;
        touch();
    }

    public void markPaymentFailed() {
        if (this.status == OrderStatus.PAID || this.status == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Cannot mark payment failed from status: " + status);
        }
        this.status = OrderStatus.PAYMENT_FAILED;
        touch();
    }

    public void markRefunded() {
        if (this.status != OrderStatus.PAID) {
            throw new IllegalStateException("Refund allowed only from PAID; current: " + status);
        }
        this.status = OrderStatus.REFUNDED;
        touch();
    }

    public void cancel() {
        if (this.status == OrderStatus.PAID || this.status == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Cannot cancel from status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        touch();
    }

    private void ensureState(OrderStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException("Expected status " + expected + ", got " + status);
        }
    }

    private void touch() { this.updatedAt = Instant.now(); }

    public void setShipping(String rateId, Long amountMinor, String currency,
                            String name, String phone,
                            String line1, String line2, String city, String state,
                            String postal, String country) {
        this.shippingRateId = rateId;
        this.shippingAmountMinor = amountMinor;
        this.shippingCurrency = currency;
        this.shippingName = name;
        this.shippingPhone = phone;
        this.shippingAddressLine1 = line1;
        this.shippingAddressLine2 = line2;
        this.shippingCity = city;
        this.shippingState = state;
        this.shippingPostalCode = postal;
        this.shippingCountry = country;
        touch();
    }
}
