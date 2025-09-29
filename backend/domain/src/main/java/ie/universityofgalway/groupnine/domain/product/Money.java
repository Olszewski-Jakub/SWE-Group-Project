package ie.universityofgalway.groupnine.domain.product;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Immutable value object representing a monetary amount in a specific currency.
 *
 * @param amount   numeric amount (scale/precision as provided)
 * @param currency ISO-4217 currency of the amount (e.g., EUR, USD)
 */
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null) {
            throw new NullPointerException("amount cannot be null");
        }
        if (currency == null) {
            throw new NullPointerException("currency cannot be null");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
    }
}
