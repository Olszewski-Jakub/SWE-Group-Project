package ie.universityofgalway.groupnine.delivery.rest.product.dto;

/**
 * A simple DTO (Data Transfer Object) to represent variant information
 * for the REST API. This is a "record" because it's just a simple data carrier.
 *
 * @param sku The Stock Keeping Unit (e.g., "CFE-ESP-SGL")
 * @param priceCents The price in cents (e.g., 250 for €2.50)
 * @param currency The ISO currency code (e.g., "EUR")
 */
public record VariantResponse(String sku, int priceCents, String currency) {}
