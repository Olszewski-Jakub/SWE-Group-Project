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

    /** Multiply this Money by an integer factor. */
    public Money multiply(int factor) {
        if (factor < 0) throw new IllegalArgumentException("factor cannot be negative");
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    /** Add another Money of the same currency. */
    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("currencies must match");
        }
        return new Money(amount.add(other.amount), currency);
    }
}
