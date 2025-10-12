package ie.universityofgalway.groupnine.domain.product;

/**
 * Sorting strategies for product search results.
 */
public enum SortRule {
    /**
     * No explicit sort is applied.
     * The backend may return results in natural order or data-source default.
     */
    DEFAULT,

    /** Order by price from low to high. */
    PRICE_LOW_TO_HIGH,

    /** Order by price from high to low. */
    PRICE_HIGH_TO_LOW,

    /** Order by newest items first (e.g., latest created/updated). */
    NEWEST_FIRST;

    /**
     * Parse a sort rule from arbitrary text.
     *
     * @param s raw sort token from external input
     * @return resolved SortRule, or DEFAULT on no match
     */
    public static SortRule parse(String s) {
        if (s == null || s.isBlank()) return DEFAULT;
        for (SortRule r : values()) {
            if (r.name().equalsIgnoreCase(s.trim())) return r;
        }
        return DEFAULT;
    }
}
