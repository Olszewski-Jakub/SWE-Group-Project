package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import java.time.Instant;
import java.util.List; // Import the List interface

/**
 * Response DTO for exposing product details over the REST API.
 * Updated to include a list of variants instead of just the count.
 */
public record ProductResponse(
    String id,
    String name,
    String description,
    String category,
    String status,
    // FIX: Changed from int variantCount to a list of VariantResponse DTOs
    List<VariantResponse> variants,
    Instant createdAt,
    Instant updatedAt
) {}
