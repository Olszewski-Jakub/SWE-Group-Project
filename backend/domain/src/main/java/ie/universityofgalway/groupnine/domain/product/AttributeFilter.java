package ie.universityofgalway.groupnine.domain.product;

import java.util.List;
/**
 Filter for one attribute with multiple acceptable values.

 @param name attribute name (e.g., "color").

 @param values allowed values for the attribute.
 */
public record AttributeFilter(String name, List<String> values) {
}
