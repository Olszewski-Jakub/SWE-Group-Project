package ie.universityofgalway.groupnine.domain.payment;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.util.Currency;
import java.util.List;
import java.util.Objects;

public final class OrderSnapshot {
    private final OrderId orderId;
    private final UserId userId;
    private final CartId cartId;
    private final List<OrderSnapshotItem> items;
    private final long totalMinor;
    private final Currency currency;

    public OrderSnapshot(OrderId orderId, UserId userId, CartId cartId,
                         List<OrderSnapshotItem> items, long totalMinor, Currency currency) {
        this.orderId = Objects.requireNonNull(orderId);
        this.userId = Objects.requireNonNull(userId);
        this.cartId = Objects.requireNonNull(cartId);
        this.items = List.copyOf(Objects.requireNonNull(items));
        this.totalMinor = totalMinor;
        this.currency = Objects.requireNonNull(currency);
    }

    public OrderId getOrderId() { return orderId; }
    public UserId getUserId() { return userId; }
    public CartId getCartId() { return cartId; }
    public List<OrderSnapshotItem> getItems() { return items; }
    public long getTotalMinor() { return totalMinor; }
    public Currency getCurrency() { return currency; }
}

