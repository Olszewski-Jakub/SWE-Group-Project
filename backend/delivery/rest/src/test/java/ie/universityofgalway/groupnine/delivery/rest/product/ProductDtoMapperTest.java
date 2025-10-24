package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.AttributeFilterDTO;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.SearchRequestDTO;
import ie.universityofgalway.groupnine.domain.product.SearchQuery;
import ie.universityofgalway.groupnine.domain.product.SortRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ProductDtoMapper.toDomain(SearchRequestDTO).
 *
 * Verifies:
 * - Trimming of key/category and corresponding hasKeyword/hasCategory flags.
 * - Defaulting: min=null -> 0; max=null -> Integer.MAX_VALUE.
 * - Sort parsing via SortRule.parse, including blank input.
 * - Mapping of AttributeFilterDTO list to non-null domain list.
 */
class ProductDtoMapperToDomainTest {

    /**
     * Happy path with explicit values; ensures trimming, price mapping,
     * sort parsing, and empty filters handling.
     */
    @Test
    @DisplayName("Happy path: trims, maps prices, parses sort")
    void toDomain_happyPath() {
        var dto = new SearchRequestDTO(
                "  latte  ",
                " coffee ",
                120,
                560,
                "PRICE_DESC",
                List.of()
        );

        SearchQuery q = ProductDtoMapper.toDomain(dto);

        assertEquals("latte", q.key());
        assertTrue(q.hasKeyword());
        assertEquals("coffee", q.category());
        assertTrue(q.hasCategory());
        assertEquals(120, q.minPriceCents());
        assertEquals(560, q.maxPriceCents());
        assertEquals(SortRule.parse("PRICE_DESC"), q.sortRule());
        assertNotNull(q.attributeFilters());
        assertTrue(q.attributeFilters().isEmpty());
    }

    /**
     * Blank key/category normalize to "" and null respectively; filters null become empty.
     */
    @Test
    @DisplayName("Blank key/category normalize; null filters -> empty")
    void toDomain_blankKeyAndCategory() {
        var dto = new SearchRequestDTO(
                "   ",
                "   ",
                0,
                0,
                "RELEVANCE",
                null
        );

        SearchQuery q = ProductDtoMapper.toDomain(dto);

        assertEquals("", q.key());
        assertFalse(q.hasKeyword());
        assertNull(q.category());
        assertFalse(q.hasCategory());
        assertNotNull(q.attributeFilters());
        assertTrue(q.attributeFilters().isEmpty());
    }

    /**
     * Null prices default to min=0 and max=Integer.MAX_VALUE; blank sort resolves via parser;
     * non-empty AttributeFilterDTO list maps to a non-empty domain list.
     */
    @Test
    @DisplayName("Null prices default; blank sort; filters mapped")
    void toDomain_nullPricesAndBlankSort() {
        var dto = new SearchRequestDTO(
                null,
                null,
                null,
                null,
                "   ",
                List.of(new AttributeFilterDTO("size", List.of("M")))
        );

        SearchQuery q = ProductDtoMapper.toDomain(dto);

        assertEquals("", q.key());
        assertFalse(q.hasKeyword());
        assertNull(q.category());
        assertFalse(q.hasCategory());
        assertEquals(0, q.minPriceCents());
        assertEquals(Integer.MAX_VALUE, q.maxPriceCents());
        assertEquals(SortRule.parse(""), q.sortRule());
        assertEquals(1, q.attributeFilters().size());
    }
}
