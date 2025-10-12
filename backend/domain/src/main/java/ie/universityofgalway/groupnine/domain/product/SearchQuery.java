package ie.universityofgalway.groupnine.domain.product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable search criteria used by the domain to query products.
 * Defaults: empty key, no category, min=0, max>=min, sort=required, filters=empty.
 */
public final class SearchQuery {
    private final String key;
    private final String category;
    private final int minPriceCents;
    private final int maxPriceCents;
    private final SortRule sortRule;
    private final List<AttributeFilter> attributeFilters;

    /**
     * Creates a new SearchQuery with normalized defaults and defensive copies.
     * key is trimmed to non-null (empty when null)
     * category is nullable and blank is treated as null
     * minPriceCents coerced to ≥ 0
     * maxPriceCents coerced to ≥ minPriceCents
     * sortRule must be non-null
     * attributeFilters is unmodifiable; null becomes empty
     */
    private SearchQuery(
            String key,
            String category,
            int minPriceCents,
            int maxPriceCents,
            SortRule sortRule,
            List<AttributeFilter> attributeFilters
    ) {
        this.key = key == null ? "" : key;
        this.category = (category == null || category.isBlank()) ? null : category;
        this.minPriceCents = Math.max(0, minPriceCents);
        this.maxPriceCents = Math.max(this.minPriceCents, maxPriceCents);
        this.sortRule = Objects.requireNonNull(sortRule, "sortRule");
        this.attributeFilters = Collections.unmodifiableList(
                attributeFilters == null ? List.of() : new ArrayList<>(attributeFilters)
        );
    }

    /**
     * Factory for building a {@link SearchQuery} with normalization and defaults.
     *
     * @param key free‑text keyword; null becomes empty, preserved otherwise
     * @param category category identifier/slug; blank becomes null
     * @param minPriceCents inclusive minimum price in cents; coerced to ≥ 0
     * @param maxPriceCents inclusive maximum price in cents; coerced to ≥ minPriceCents
     * @param sortRule non-null sort rule applied by repositories
     * @param attributeFilters list of {@link AttributeFilter}; null becomes empty, defensively copied
     * @return normalized {@link SearchQuery}
     * @throws NullPointerException if sortRule is null
     */
    public static SearchQuery builder(
            String key,
            String category,
            int minPriceCents,
            int maxPriceCents,
            SortRule sortRule,
            List<AttributeFilter> attributeFilters
    ) {
        return new SearchQuery(key, category, minPriceCents, maxPriceCents, sortRule, attributeFilters);
    }

    /** Free‑text keyword (never null; may be empty). */
    public String key() { return key; }

    /** Category identifier/slug; null means no category filter. */
    public String category() { return category; }

    /** Inclusive minimum price in cents (always ≥ 0). */
    public int minPriceCents() { return minPriceCents; }

    /** Inclusive maximum price in cents (always ≥ minPriceCents). */
    public int maxPriceCents() { return maxPriceCents; }

    /** Sort rule to apply (never null). */
    public SortRule sortRule() { return sortRule; }

    /** Unmodifiable list of attribute filters (never null; may be empty). */
    public List<AttributeFilter> attributeFilters() { return attributeFilters; }

    /** True when a category filter is present. */
    public boolean hasCategory() { return category != null; }

    /** True when the keyword is non-blank. */
    public boolean hasKeyword() { return !key.isBlank(); }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "key='" + key + '\'' +
                ", category='" + category + '\'' +
                ", minPriceCents=" + minPriceCents +
                ", maxPriceCents=" + maxPriceCents +
                ", sortRule=" + sortRule +
                ", attributeFilters=" + attributeFilters +
                '}';
    }
}
