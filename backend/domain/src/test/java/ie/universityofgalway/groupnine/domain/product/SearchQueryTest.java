package ie.universityofgalway.groupnine.domain.product;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchQueryTest {
    @Test
    void builder_normalizes_defaults_and_bounds() {
        SearchQuery q = SearchQuery.builder(null, " ", -10, 5, SortRule.DEFAULT, null);
        assertEquals("", q.key());
        assertNull(q.category());
        assertEquals(0, q.minPriceCents());
        assertEquals(5, q.maxPriceCents());
        assertEquals(SortRule.DEFAULT, q.sortRule());
        assertTrue(q.attributeFilters().isEmpty());
        assertFalse(q.hasCategory());
        assertFalse(q.hasKeyword());
    }

    @Test
    void max_is_coerced_to_at_least_min() {
        SearchQuery q = SearchQuery.builder("k", "cat", 100, 50, SortRule.NEWEST_FIRST, List.of());
        assertEquals(100, q.minPriceCents());
        assertEquals(100, q.maxPriceCents());
        assertEquals("k", q.key());
        assertEquals("cat", q.category());
        assertTrue(q.hasKeyword());
        assertTrue(q.hasCategory());
    }

    @Test
    void attributeFilters_is_unmodifiable_copy() {
        var filters = new java.util.ArrayList<AttributeFilter>();
        filters.add(new AttributeFilter("color", List.of("Blue")));
        SearchQuery q = SearchQuery.builder("k", null, 0, 0, SortRule.DEFAULT, filters);
        assertEquals(1, q.attributeFilters().size());
        assertThrows(UnsupportedOperationException.class, () -> q.attributeFilters().add(new AttributeFilter("size", List.of("L"))));
        filters.add(new AttributeFilter("size", List.of("L")));
        assertEquals(1, q.attributeFilters().size(), "defensive copy should prevent external mutation");
    }

    @Test
    void sortRule_must_be_nonNull() {
        assertThrows(NullPointerException.class, () -> SearchQuery.builder("k", null, 0, 0, null, List.of()));
    }
}

