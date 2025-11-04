package ie.universityofgalway.groupnine.service.payments.port.dto;

/**
 * Immutable value object describing a single line item to include when creating
 * a hosted payment session with a payment gateway. Amounts are expressed in
 * minor currency units (for example, cents) to avoid rounding issues.
 * <p>
 * Instances of this class are created by application services when invoking
 * {@link ie.universityofgalway.groupnine.service.payments.port.PaymentGatewayPort}.
 */
public final class CheckoutLineItem {
    private final String name;
    private final long unitAmountMinor;
    private final int quantity;

    /**
     * Creates a new line item definition.
     *
     * @param name            human‑readable display name as seen by customers (must not be null)
     * @param unitAmountMinor price per single unit in minor currency units (e.g., cents)
     * @param quantity        number of units to purchase (must be {@code > 0})
     */
    public CheckoutLineItem(String name, long unitAmountMinor, int quantity) {
        this.name = name;
        this.unitAmountMinor = unitAmountMinor;
        this.quantity = quantity;
    }

    /**
     * @return the customer‑visible display name
     */
    public String getName() { return name; }

    /**
     * @return the price per unit in minor currency units (for example, cents)
     */
    public long getUnitAmountMinor() { return unitAmountMinor; }

    /**
     * @return the number of units to purchase
     */
    public int getQuantity() { return quantity; }
}
