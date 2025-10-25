package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductResponse;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.SearchRequestDTO;
import ie.universityofgalway.groupnine.domain.product.AttributeFilter;
import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.SortRule;
import ie.universityofgalway.groupnine.domain.product.SearchQuery;

import java.util.List;

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
        p.id() == null ? null : p.id().id().toString(),
        p.name(),
        p.description(),
        p.category(),
        p.status() == null ? null : p.status().name(),
        p.variants() == null ? 0 : p.variants().size(),
        p.createdAt(),
        p.updatedAt()
    );
  }

  /**
   * Maps a REST {@link SearchRequestDTO} to the domain {@link SearchQuery}.
   * Null minPrice → 0; null maxPrice → Integer.MAX_VALUE.
   * {@link SortRule} is parsed case-insensitively; unknown/blank → DEFAULT.
   * Null attributeFilters → empty list.
   */
  public static SearchQuery toDomain(SearchRequestDTO dto) {
    return SearchQuery.builder(
            dto.key() == null ? null : dto.key().trim(),
            dto.category() == null ? null : dto.category().trim(),
            dto.minPriceCents() == null ? 0 : dto.minPriceCents(),
            dto.maxPriceCents() == null ? Integer.MAX_VALUE : dto.maxPriceCents(),
            SortRule.parse(dto.sortRule()),
            dto.attributeFilters() == null ? List.of()
                    : dto.attributeFilters().stream()
                    .map(a -> new AttributeFilter(a.name(), a.values()))
                    .toList()
    );
  }
}
