package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import java.util.List;

/**
 * A Data Transfer Object (DTO) representing product variant information for the REST API.
 * As a record, this class is a simple, immutable data carrier.
 *
 * @param id         The unique identifier of the variant.
 * @param sku        The Stock Keeping Unit (e.g., "CFE-ESP-SGL").
 * @param priceCents The price of the variant in cents (e.g., 250 for €2.50).
 * @param currency   The ISO 4217 currency code (e.g., "EUR").
 * @param imageUrl   Public URL to fetch the variant's image, if available.
 * @param attributes Attributes of the variant (e.g, [{"name": "roast", "value": "medium"}, {"name": "origin", "value": "Brazil"}])
 */
public record VariantResponse(String id, String sku, int priceCents, String currency, String imageUrl, List<AttributeDto> attributes) {}
