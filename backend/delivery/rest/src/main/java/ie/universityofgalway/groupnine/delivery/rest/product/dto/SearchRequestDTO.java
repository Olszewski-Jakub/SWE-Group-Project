package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import java.util.List;

/**
 *  * Request payload for product search.
 *  * All fields are optional; missing values imply no filtering for that aspect.
 *
 * @param key
 * @param category
 * @param minPriceCents
 * @param maxPriceCents
 * @param sortRule
 * @param attributeFilters
 */
public record SearchRequestDTO(
        String key,
        String category,
        Integer minPriceCents,
        Integer maxPriceCents,
        String sortRule,
        List<AttributeFilterDTO> attributeFilters
) {}