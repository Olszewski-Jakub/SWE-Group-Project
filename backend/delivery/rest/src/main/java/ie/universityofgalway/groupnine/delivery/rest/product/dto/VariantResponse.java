package ie.universityofgalway.groupnine.delivery.rest.product.dto;

/**
 * A Data Transfer Object (DTO) representing product variant information for the REST API.
 * As a record, this class is a simple, immutable data carrier.
 *
 * @param sku        The Stock Keeping Unit (e.g., "CFE-ESP-SGL").
 * @param priceCents The price of the variant in cents (e.g., 250 for €2.50).
 * @param currency   The ISO 4217 currency code (e.g., "EUR").
 */
public record VariantResponse(String sku, int priceCents, String currency) {}