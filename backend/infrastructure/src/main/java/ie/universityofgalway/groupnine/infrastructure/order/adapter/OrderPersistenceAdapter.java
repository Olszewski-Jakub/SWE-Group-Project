package ie.universityofgalway.groupnine.infrastructure.order.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.order.Order;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.order.OrderStatus;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.order.jpa.OrderEntity;
import ie.universityofgalway.groupnine.infrastructure.order.jpa.OrderJpaRepository;
import ie.universityofgalway.groupnine.service.order.port.OrderPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

/**
 * JPA adapter implementing the {@link ie.universityofgalway.groupnine.service.order.port.OrderPort}.
 * Maps between domain {@code Order} aggregates and {@code OrderEntity} rows including shipping fields.
 */
@Component
public class OrderPersistenceAdapter implements OrderPort {
    private final OrderJpaRepository repo;

    public OrderPersistenceAdapter(OrderJpaRepository repo) {
        this.repo = repo;
    }

    /** Persists an order and returns the mapped domain aggregate. */
    @Override
    public Order save(Order order) {
        OrderEntity e = toEntity(order);
        OrderEntity saved = repo.save(e);
        return toDomain(saved);
    }

    /** Finds an order by id and maps it to the domain aggregate. */
    @Override
    public Optional<Order> findById(OrderId id) {
        return repo.findById(id.value()).map(this::toDomain);
    }

    private OrderEntity toEntity(Order o) {
        OrderEntity e = new OrderEntity();
        e.setId(o.getId().value());
        e.setUserId(o.getUserId().value());
        e.setCartId(o.getCartId().getId());
        e.setTotalMinor(toMinor(o.getTotal().getAmount(), o.getTotal().getCurrency()));
        e.setCurrency(o.getTotal().getCurrency().getCurrencyCode());
        e.setStatus(o.getStatus().name());
        e.setSnapshotJson(null);
        e.setCreatedAt(o.getCreatedAt());
        e.setUpdatedAt(o.getUpdatedAt());
        e.setShippingRateId(o.getShippingRateId());
        e.setShippingAmountMinor(o.getShippingAmountMinor());
        e.setShippingCurrency(o.getShippingCurrency());
        e.setShippingName(o.getShippingName());
        e.setShippingPhone(o.getShippingPhone());
        e.setShippingAddressLine1(o.getShippingAddressLine1());
        e.setShippingAddressLine2(o.getShippingAddressLine2());
        e.setShippingCity(o.getShippingCity());
        e.setShippingState(o.getShippingState());
        e.setShippingPostalCode(o.getShippingPostalCode());
        e.setShippingCountry(o.getShippingCountry());
        return e;
    }

    private Order toDomain(OrderEntity e) {
        Money total = new Money(new BigDecimal(e.getTotalMinor()).movePointLeft(Currency.getInstance(e.getCurrency()).getDefaultFractionDigits()), Currency.getInstance(e.getCurrency()));
        Order o = new Order(new OrderId(e.getId()), UserId.of(e.getUserId()), new CartId(e.getCartId()), total,
                OrderStatus.valueOf(e.getStatus()), e.getCreatedAt(), e.getUpdatedAt());
        o.setShipping(e.getShippingRateId(), e.getShippingAmountMinor(), e.getShippingCurrency(),
                e.getShippingName(), e.getShippingPhone(),
                e.getShippingAddressLine1(), e.getShippingAddressLine2(), e.getShippingCity(), e.getShippingState(),
                e.getShippingPostalCode(), e.getShippingCountry());
        return o;
    }

    private static long toMinor(BigDecimal amount, Currency currency) {
        int fraction = currency.getDefaultFractionDigits();
        return amount.movePointRight(Math.max(fraction, 0)).longValueExact();
    }
}
