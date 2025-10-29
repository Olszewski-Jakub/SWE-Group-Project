package ie.universityofgalway.groupnine.domain.product;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Money} value object.
 */
class MoneyTest {
    private final Currency EUR = Currency.getInstance("EUR");
    private final Currency USD = Currency.getInstance("USD");

    /**
     * Verifies that the {@link Money} constructor correctly validates its inputs,
     * rejecting null or negative amounts and null currencies.
     */
    @Test
    void constructor_validatesInputs() {
        assertThrows(NullPointerException.class, () -> new Money(null, EUR));
        assertThrows(NullPointerException.class, () -> new Money(BigDecimal.ZERO, null));
        assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("-1.00"), EUR));

        Money m = new Money(new BigDecimal("0.00"), EUR);
        assertEquals(new BigDecimal("0.00"), m.getAmount());
        assertEquals(EUR, m.getCurrency());
    }

    /**
     * Tests the mathematical operations of the {@link Money} class, including
     * multiplication and addition, and ensures currency consistency is enforced.
     */
    @Test
    void multiply_and_add_behaviour() {
        Money ten = new Money(new BigDecimal("10.00"), EUR);
        Money twenty = ten.multiply(2);
        assertEquals(new Money(new BigDecimal("20.00"), EUR), twenty);
        assertThrows(IllegalArgumentException.class, () -> ten.multiply(-1));

        Money sum = ten.add(new Money(new BigDecimal("5.00"), EUR));
        assertEquals(new Money(new BigDecimal("15.00"), EUR), sum);
        assertThrows(IllegalArgumentException.class, () -> ten.add(new Money(BigDecimal.ONE, USD)));
    }
}