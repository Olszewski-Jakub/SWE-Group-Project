package ie.universityofgalway.groupnine.domain.product;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable value object representing a monetary amount in a specific currency.
 * Converted from a record to a class.
 */
public final class Money {

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new NullPointerException("amount cannot be null");
        }
        if (currency == null) {
            throw new NullPointerException("currency cannot be null");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        this.amount = amount;
        this.currency = currency;
    }

    // Getters
    public BigDecimal getAmount() { return amount; }
    public Currency getCurrency() { return currency; }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.equals(money.amount) && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return "Money[amount=" + amount + ", currency=" + currency + ']';
    }
}
