package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.AttributeDto;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductResponse;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.SearchRequestDTO;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantResponse;
import ie.universityofgalway.groupnine.domain.product.AttributeFilter;
import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.SearchQuery;
import ie.universityofgalway.groupnine.domain.product.SortRule;
import java.util.List;

/**
 * A utility class for mapping between product-related domain objects and their
 * corresponding Data Transfer Objects (DTOs) for the REST layer.
 */
public final class ProductDtoMapper {

  private ProductDtoMapper() {}

  /**
   * Maps a {@link Product} domain object to a {@link ProductResponse} DTO.
   *
   * @param p The Product domain object to map.
   * @return The resulting ProductResponse DTO.
   */
  public static ProductResponse toDto(Product p) {
    List<VariantResponse> variantDTOs = p.getVariants().stream()
        .map(v -> new VariantResponse(
            v.getId() == null ? null : v.getId().getId().toString(),
            v.getSku().getValue(),
            v.getPrice().getAmount().multiply(new java.math.BigDecimal("100")).intValue(),
            v.getPrice().getCurrency().getCurrencyCode(),
            v.getImageUrl(),
            v.getAttributes() == null ? List.of()
                    : v.getAttributes().stream()
                    .map(a -> new AttributeDto(a.name(), a.value()))
                    .toList()
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
   * Maps a {@link SearchRequestDTO} from the REST layer to a {@link SearchQuery}
   * domain object, normalizing inputs such as trimming whitespace from the key and category.
   *
   * @param dto The SearchRequestDTO to map.
   * @return The resulting SearchQuery domain object.
   */
  public static SearchQuery toDomain(SearchRequestDTO dto) {
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
