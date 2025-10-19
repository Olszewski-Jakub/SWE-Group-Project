package ie.universityofgalway.groupnine.domain.product;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SearchQuery}.
 *
 * Validates normalization rules, default assignments, immutability of collections,
 * and correct behavior of helper and utility methods.
 */
class SearchQueryTest {

    /**
     * Verifies normalization logic and default rules:
     * - null keyword becomes ""
     * - blank category becomes null
     * - minPriceCents less than 0 coerced to 0
     * - maxPriceCents less than minPriceCents coerced to min
     * - sortRule must be non-null
     * - attributeFilters list is defensively copied and unmodifiable
     */
    @Test
    void builder_normalizes_inputs_and_applies_defaults() {
        List<AttributeFilter> input = new ArrayList<>();
        input.add(new AttributeFilter("color", List.of("red")));

        SearchQuery q = SearchQuery.builder(
                null, // key → ""
                "   ", // category → null
                -5, // min → 0
                -1, // max → coerced to 0
                SortRule.DEFAULT,
                input
        );

        assertEquals("", q.key()); // null key normalized
        assertNull(q.category()); // blank category normalized
        assertEquals(0, q.minPriceCents()); // negative min → 0
        assertEquals(0, q.maxPriceCents()); // max coerced to min
        assertEquals(SortRule.DEFAULT, q.sortRule()); // non-null sort required
        assertEquals(1, q.attributeFilters().size()); // copied defensively

        // Source list mutation must not affect internal state
        input.add(new AttributeFilter("size", List.of("L")));
        assertEquals(1, q.attributeFilters().size());

        // Returned list must be unmodifiable
        assertThrows(UnsupportedOperationException.class,
                () -> q.attributeFilters().add(new AttributeFilter("size", List.of("L"))));
    }

    /**
     * Validates that helper methods correctly reflect normalized state.
     */
    @Test
    void helpers_reflect_normalized_state() {
        SearchQuery a = SearchQuery.builder("", "", 0, 0, SortRule.DEFAULT, null);
        assertFalse(a.hasCategory()); // blank → null → false
        assertFalse(a.hasKeyword()); // empty key → false

        SearchQuery b = SearchQuery.builder("  coffee  ", "kitchen", 1, 1, SortRule.NEWEST_FIRST, List.of());
        assertTrue(b.hasCategory()); // non-blank category
        assertTrue(b.hasKeyword()); // non-blank key
        assertEquals("  coffee  ", b.key()); // key preserved (not trimmed)
    }

    /**
     * Ensures sortRule is mandatory and throws NullPointerException if missing,
     * with an informative message.
     */
    @Test
    void sortRule_is_required() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> SearchQuery.builder("x", null, 0, 0, null, null));
        assertTrue(ex.getMessage().contains("sortRule"));
    }

    /**
     * Ensures maxPriceCents is coerced up to minPriceCents if smaller,
     * and preserved otherwise.
     */
    @Test
    void maxPrice_coerces_to_min_when_smaller() {
        SearchQuery q1 = SearchQuery.builder("k", null, 200, 100, SortRule.DEFAULT, null);
        assertEquals(200, q1.minPriceCents());
        assertEquals(200, q1.maxPriceCents());

        SearchQuery q2 = SearchQuery.builder("k", null, 200, 300, SortRule.DEFAULT, null);
        assertEquals(300, q2.maxPriceCents()); // preserved when ≥ min
    }

    /**
     * Verifies that toString() output includes key identifying fields
     * for debugging clarity.
     */
    @Test
    void toString_contains_key_fields() {
        SearchQuery q = SearchQuery.builder("x", "cat", 1, 2, SortRule.PRICE_LOW_TO_HIGH, List.of());
        String s = q.toString();
        assertTrue(s.contains("key='x'"));
        assertTrue(s.contains("category='cat'"));
        assertTrue(s.contains("minPriceCents=1"));
        assertTrue(s.contains("maxPriceCents=2"));
        assertTrue(s.contains("PRICE_LOW_TO_HIGH"));
    }
}
