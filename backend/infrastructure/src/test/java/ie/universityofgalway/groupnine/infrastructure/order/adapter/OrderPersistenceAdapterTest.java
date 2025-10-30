package ie.universityofgalway.groupnine.infrastructure.order.adapter;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.order.Order;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.order.OrderStatus;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.infrastructure.order.jpa.OrderEntity;
import ie.universityofgalway.groupnine.infrastructure.order.jpa.OrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrderPersistenceAdapterTest {

    private OrderJpaRepository repo;
    private OrderPersistenceAdapter adapter;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(OrderJpaRepository.class);
        adapter = new OrderPersistenceAdapter(repo);
    }

    private Order sampleOrder() {
        OrderId oid = OrderId.of(UUID.randomUUID());
        UserId uid = UserId.of(UUID.randomUUID());
        CartId cid = CartId.of(UUID.randomUUID());
        Money total = new Money(new BigDecimal("12.34"), Currency.getInstance("EUR"));
        Order o = new Order(oid, uid, cid, total, OrderStatus.PENDING_PAYMENT, Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"));
        o.setShipping("shr_123", 499L, "EUR", "Alice", "+123", "L1", null, "City", "State", "P1", "IE");
        return o;
    }

    @Test
    void save_maps_domain_to_entity_and_back() {
        Order o = sampleOrder();

        // Echo back whatever entity we receive (simulate JPA save)
        ArgumentCaptor<OrderEntity> cap = ArgumentCaptor.forClass(OrderEntity.class);
        when(repo.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Order saved = adapter.save(o);

        // Verify round-trip values
        assertEquals(o.getId(), saved.getId());
        assertEquals(o.getUserId(), saved.getUserId());
        assertEquals(o.getCartId(), saved.getCartId());
        assertEquals(o.getTotal().getAmount(), saved.getTotal().getAmount());
        assertEquals(o.getTotal().getCurrency(), saved.getTotal().getCurrency());
        assertEquals(o.getStatus(), saved.getStatus());
        assertEquals("shr_123", saved.getShippingRateId());
        assertEquals(499L, saved.getShippingAmountMinor());
        assertEquals("Alice", saved.getShippingName());
        assertEquals("IE", saved.getShippingCountry());
    }

    @Test
    void findById_maps_entity_to_domain() {
        Order o = sampleOrder();
        // Build an entity matching the domain order
        OrderEntity e = new OrderEntity();
        e.setId(o.getId().value());
        e.setUserId(o.getUserId().value());
        e.setCartId(o.getCartId().getId());
        e.setTotalMinor(1234);
        e.setCurrency("EUR");
        e.setStatus(o.getStatus().name());
        e.setCreatedAt(o.getCreatedAt());
        e.setUpdatedAt(o.getUpdatedAt());
        e.setShippingRateId("shr_123");
        e.setShippingAmountMinor(499L);
        e.setShippingCurrency("EUR");
        e.setShippingName("Alice");
        e.setShippingPhone("+123");
        e.setShippingAddressLine1("L1");
        e.setShippingCity("City");
        e.setShippingState("State");
        e.setShippingPostalCode("P1");
        e.setShippingCountry("IE");

        when(repo.findById(o.getId().value())).thenReturn(Optional.of(e));

        var found = adapter.findById(o.getId()).orElseThrow();
        assertEquals(o.getId(), found.getId());
        assertEquals(o.getUserId(), found.getUserId());
        assertEquals(o.getCartId(), found.getCartId());
        assertEquals(new BigDecimal("12.34"), found.getTotal().getAmount());
        assertEquals(Currency.getInstance("EUR"), found.getTotal().getCurrency());
        assertEquals("shr_123", found.getShippingRateId());
        assertEquals(499L, found.getShippingAmountMinor());
        assertEquals("IE", found.getShippingCountry());
    }
}

