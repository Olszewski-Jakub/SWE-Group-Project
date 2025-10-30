package ie.universityofgalway.groupnine.service.payments.port.dto;

/**
 * Encapsulates optional shipping configuration to be applied when creating a
 * Checkout session. Both lists are immutable snapshots to ensure safe sharing
 * across threads and layers.
 */
public final class ShippingOptions {
    private final java.util.List<String> allowedCountries;
    private final java.util.List<String> shippingRateIds;

    /**
     * Creates a new options container.
     *
     * @param allowedCountries two‑letter ISO country codes eligible for shipping address collection
     * @param shippingRateIds  provider‑specific shipping rate identifiers to present to customers
     */
    public ShippingOptions(java.util.List<String> allowedCountries, java.util.List<String> shippingRateIds) {
        this.allowedCountries = allowedCountries == null ? java.util.List.of() : java.util.List.copyOf(allowedCountries);
        this.shippingRateIds = shippingRateIds == null ? java.util.List.of() : java.util.List.copyOf(shippingRateIds);
    }

    /** @return immutable list of allowed country codes; empty means no restriction */
    public java.util.List<String> getAllowedCountries() { return allowedCountries; }
    /** @return immutable list of shipping rate identifiers; empty means none configured */
    public java.util.List<String> getShippingRateIds() { return shippingRateIds; }
}
