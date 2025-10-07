package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductResponse;
import ie.universityofgalway.groupnine.domain.product.Product;

/**
 * Mapper for converting domain {@link Product} objects into REST-layer
 * {@link ProductResponse} DTOs.

 *
 * Use from controllers/services to prepare response payloads without leaking
 * domain internals to the API surface.
 */
public final class ProductDtoMapper {
  private ProductDtoMapper() {}

  /**
   * Maps a domain {@link Product} to a {@link ProductResponse}.
   */
  public static ProductResponse toDto(Product p) {
    return new ProductResponse(
        p.id() == null ? null : p.id().toString(),
        p.name(),
        p.description(),
        p.category(),
        p.status() == null ? null : p.status().name(),
        p.variants() == null ? 0 : p.variants().size(),
        p.createdAt(),
        p.updatedAt()
    );
  }
}
