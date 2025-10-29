package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import java.time.Instant;
import java.util.List;

/**
 * A Data Transfer Object (DTO) for exposing product details over the REST API.
 *
 * @param id          The unique identifier of the product.
 * @param name        The name of the product.
 * @param description A brief description of the product.
 * @param category    The category the product belongs to.
 * @param status      The current status of the product (e.g., "AVAILABLE", "DRAFT").
 * @param variants    A list of {@link VariantResponse} objects associated with this product.
 * @param createdAt   The timestamp when the product was created.
 * @param updatedAt   The timestamp when the product was last updated.
 */
public record ProductResponse(
    String id,
    String name,
    String description,
    String category,
    String status,
    List<VariantResponse> variants,
    Instant createdAt,
    Instant updatedAt
) {}