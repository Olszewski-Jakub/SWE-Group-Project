package ie.universityofgalway.groupnine.domain.order;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.user.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order newPendingOrder() {
        Money total = new Money(new BigDecimal("12.34"), Currency.getInstance("EUR"));
        return new Order(OrderId.newId(), UserId.newId(), new CartId(java.util.UUID.randomUUID()), total,
                OrderStatus.PENDING_PAYMENT, Instant.now(), Instant.now());
    }

    @Test
    void createPending_hasPendingStatus() {
        Money total = new Money(new BigDecimal("9.99"), Currency.getInstance("EUR"));
        Order o = Order.createPending(UserId.newId(), new CartId(java.util.UUID.randomUUID()), total);
        assertEquals(OrderStatus.PENDING_PAYMENT, o.getStatus());
        assertEquals(total, o.getTotal());
    }

    @Test
    void markPaid_transitionsFromPending() {
        Order o = newPendingOrder();
        o.markPaid();
        assertEquals(OrderStatus.PAID, o.getStatus());
    }

    @Test
    void markPaid_throws_when_notPending() {
        Order o = newPendingOrder();
        o.markPaymentFailed();
        assertThrows(IllegalStateException.class, o::markPaid);
    }

    @Test
    void paymentFailed_fromPendingAllowed() {
        Order o = newPendingOrder();
        o.markPaymentFailed();
        assertEquals(OrderStatus.PAYMENT_FAILED, o.getStatus());
    }

    @Test
    void paymentFailed_fromPaidThrows() {
        Order o = newPendingOrder();
        o.markPaid();
        assertThrows(IllegalStateException.class, o::markPaymentFailed);
    }

    @Test
    void refunded_onlyFromPaid() {
        Order o = newPendingOrder();
        assertThrows(IllegalStateException.class, o::markRefunded);
        o.markPaid();
        o.markRefunded();
        assertEquals(OrderStatus.REFUNDED, o.getStatus());
    }

    @Test
    void cancel_notAllowedFromPaidOrRefunded() {
        Order o = newPendingOrder();
        o.cancel();
        assertEquals(OrderStatus.CANCELLED, o.getStatus());
        Order o2 = newPendingOrder();
        o2.markPaid();
        assertThrows(IllegalStateException.class, o2::cancel);
    }

    @Test
    void setShipping_populatesFields_andUpdatesTimestamp() {
        Order o = newPendingOrder();
        Instant before = o.getUpdatedAt();
        o.setShipping("shr_123", 599L, "EUR", "John Doe", "+353123",
                "Line1", "Line2", "Galway", "Co. Galway", "H91", "IE");
        assertEquals("shr_123", o.getShippingRateId());
        assertEquals(599L, o.getShippingAmountMinor());
        assertEquals("EUR", o.getShippingCurrency());
        assertEquals("John Doe", o.getShippingName());
        assertNotEquals(before, o.getUpdatedAt());
    }
}

