package ie.universityofgalway.groupnine.domain.order;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderIdTest {
    @Test
    void equality_onUnderlyingUuid() {
        UUID u = UUID.randomUUID();
        OrderId a = OrderId.of(u);
        OrderId b = OrderId.of(u);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(u.toString(), a.toString());
    }
}

