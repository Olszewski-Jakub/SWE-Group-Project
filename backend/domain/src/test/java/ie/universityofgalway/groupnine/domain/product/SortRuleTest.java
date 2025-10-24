package ie.universityofgalway.groupnine.domain.product;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SortRule#parse(String)} covering case-insensitivity, trimming, and defaults.
 */
class SortRuleTest {

    /**
     * Should parse enum names ignoring case and surrounding spaces, defaulting on null/blank/unknown.
     */
    @Test
    void parse_handles_case_spaces_and_unknowns() {
        assertEquals(SortRule.DEFAULT, SortRule.parse(null)); // null -> DEFAULT
        assertEquals(SortRule.DEFAULT, SortRule.parse("   ")); // blank -> DEFAULT
        assertEquals(SortRule.PRICE_LOW_TO_HIGH, SortRule.parse("PRICE_LOW_TO_HIGH")); // exact
        assertEquals(SortRule.PRICE_LOW_TO_HIGH, SortRule.parse(" price_low_to_high ")); // case/trim
        assertEquals(SortRule.DEFAULT, SortRule.parse("unknown")); // fallback
        assertEquals(SortRule.NEWEST_FIRST, SortRule.parse("Newest_First")); // case-insensitive
    }
}
