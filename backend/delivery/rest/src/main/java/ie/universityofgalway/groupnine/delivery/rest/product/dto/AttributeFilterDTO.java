package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import java.util.List;

/**
 * Single attribute filter sent in search requests.
 * Example: name = "color", values = ["Black","Blue"].
 * @param name
 * @param values
 */
public record AttributeFilterDTO(
        String name,
        List<String> values
) {
}
