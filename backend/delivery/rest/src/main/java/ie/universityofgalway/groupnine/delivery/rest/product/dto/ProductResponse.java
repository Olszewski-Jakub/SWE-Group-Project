package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import java.time.Instant;
/**
 * Response DTO for exposing product details over the REST API.
 * 
 * This is a presentation-layer shape (not the domain model). It is purposely
 */
public record ProductResponse(
    String id,
    String name,
    String description,
    String category,
    String status,
    int variantCount,
    Instant createdAt,
    Instant updatedAt
) {}
