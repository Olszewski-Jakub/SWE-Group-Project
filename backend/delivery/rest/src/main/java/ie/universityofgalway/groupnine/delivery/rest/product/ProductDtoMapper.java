package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductResponse;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.SearchRequestDTO;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantResponse;
import ie.universityofgalway.groupnine.domain.product.AttributeFilter;
import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.SortRule;
import ie.universityofgalway.groupnine.domain.product.SearchQuery;

import java.util.List;

/**
 * Mapper for converting domain {@link Product} objects into REST-layer
 * {@link ProductResponse} DTOs and vice-versa.
 */
public final class ProductDtoMapper {
  private ProductDtoMapper() {}

  /**
   * Maps a domain {@link Product} to a {@link ProductResponse}.
   */
  public static ProductResponse toDto(Product p) {
    var variantDTOs = p.getVariants().stream()
        .map(v -> new VariantResponse(
            v.getSku().getValue(),
            v.getPrice().getAmount().multiply(new java.math.BigDecimal("100")).intValue(),
            v.getPrice().getCurrency().getCurrencyCode()
        ))
        .toList();

    return new ProductResponse(
        p.getId() == null ? null : p.getId().getId().toString(),
        p.getName(),
        p.getDescription(),
        p.getCategory(),
        p.getStatus() == null ? null : p.getStatus().name(),
        variantDTOs,
        p.getCreatedAt(),
        p.getUpdatedAt()
    );
  }

  /**
   * Maps a REST {@link SearchRequestDTO} to the domain {@link SearchQuery},
   * trimming whitespace from key and category.
   */
  public static SearchQuery toDomain(SearchRequestDTO dto) {
    // FIX: Trim whitespace from key and category before creating the domain query.
    String key = (dto.key() != null) ? dto.key().trim() : null;
    String category = (dto.category() != null) ? dto.category().trim() : null;

    return SearchQuery.builder(
            key,
            category,
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