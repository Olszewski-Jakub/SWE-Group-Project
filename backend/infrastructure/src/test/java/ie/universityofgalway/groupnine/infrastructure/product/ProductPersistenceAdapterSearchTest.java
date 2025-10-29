package ie.universityofgalway.groupnine.infrastructure.product;

import ie.universityofgalway.groupnine.domain.product.AttributeFilter;
import ie.universityofgalway.groupnine.domain.product.SearchQuery;
import ie.universityofgalway.groupnine.domain.product.SortRule;
import ie.universityofgalway.groupnine.infrastructure.product.adapter.ProductPersistenceAdapter;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.ProductJpaRepository;
import ie.universityofgalway.groupnine.infrastructure.product.jpa.VariantJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ProductPersistanceAdapterSearchTest
 *
 * Purpose:
 * - Unit-test the adapter’s “parameter shaping” logic before delegating to the repository.
 * - This is not an SQL/integration test; it verifies only:
 *   - Price bound normalization (min==0 -> null, max==Integer.MAX_VALUE -> null).
 *   - Sort token generation (String.valueOf(enum)).
 *   - Attribute filter JSON assembly (only when present, with minimal escaping).
 *   - Straight-through forwarding of category/key and Pageable to the repository.
 *
 * Why:
 * - Keeps adapter responsibilities well-tested without a database.
 * - Leaves SQL behavior and JSONB/pg_trgm specifics to integration tests.
 */
public class ProductPersistenceAdapterSearchTest {

    /** Helper to construct the system under test with a mocked repository. */
    private ProductPersistenceAdapter adapterWith(ProductJpaRepository repo, VariantJpaRepository variantRepo) {
        return new ProductPersistenceAdapter(repo, variantRepo);
    }

    /** Convenience overload for tests that do not need variant repository. */
    private ProductPersistenceAdapter adapterWith(ProductJpaRepository repo) {
        return new ProductPersistenceAdapter(repo);
    }

    /**
     * Verifies the core transformation block:
     * - 0 -> null for minPrice, Integer.MAX_VALUE -> null for maxPrice.
     * - Enum -> String for sort.
     * - Attribute filters are converted into a compact JSON object string.
     * - All parameters and the pageable are forwarded to the repository call.
     */
    @Test
    @DisplayName("normalizes prices, builds attr JSON, delegates")
    void normalizesPrices_buildsAttrJson_delegates() {
        ProductJpaRepository repo = mock(ProductJpaRepository.class);
        when(repo.search(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of())); // returned Page is not the focus here

        var adapter = adapterWith(repo);

        // Build a query that triggers normalization and JSON assembly
        SearchQuery q = SearchQuery.builder(
                "latte", // key -> forwarded
                "coffee", // category -> forwarded
                0, // min -> becomes null in adapter
                Integer.MAX_VALUE, // max -> becomes null in adapter
                SortRule.PRICE_LOW_TO_HIGH, // sort -> enum name string
                List.of(
                        new AttributeFilter("milk", List.of("oat", "so\"y")), // test minimal escaping
                        new AttributeFilter("size", List.of("medium"))
                )
        );

        adapter.search(q, PageRequest.of(0, 10));

        // Capture what the adapter sent to the repository
        ArgumentCaptor<Integer> minCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> maxCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> sortCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> attrCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> catCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> simCap = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Pageable> pageableCap = ArgumentCaptor.forClass(Pageable.class);

        verify(repo).search(
                catCap.capture(),
                keyCap.capture(),
                minCap.capture(),
                maxCap.capture(),
                sortCap.capture(),
                simCap.capture(),
                attrCap.capture(),
                pageableCap.capture()
        );

        // Price normalization
        assertNull(minCap.getValue()); // 0 -> null
        assertNull(maxCap.getValue()); // MAX -> null

        // Sort token and basic forwarding
        assertEquals("PRICE_LOW_TO_HIGH", sortCap.getValue());
        assertEquals("coffee", catCap.getValue());
        assertEquals("latte", keyCap.getValue());

        // Similarity cutoff constant from adapter
        assertEquals(0.14, simCap.getValue(), 1e-9);

        // Attribute JSON shape and escaping
        String json = attrCap.getValue();
        assertNotNull(json);
        assertTrue(json.contains("\"milk\""));
        assertTrue(json.contains("\"oat\""));
        assertTrue(json.contains("so\\\"y")); // embedded quote escaped
        assertTrue(json.contains("\"size\""));
        assertTrue(json.contains("\"medium\""));

        // Pageable preserved
        assertEquals(0, pageableCap.getValue().getPageNumber());
        assertEquals(10, pageableCap.getValue().getPageSize());
    }

    /**
     * When no attribute filters are provided:
     * - attrJson must be null (adapter skips JSON assembly).
     * - Explicit, non-default price bounds must be forwarded unchanged.
     * - Sort token must match the enum name.
     */
    @Test
    @DisplayName("no attributes -> attr null; explicit bounds kept")
    void noAttributes_attrNull_boundsKept() {
        ProductJpaRepository repo = mock(ProductJpaRepository.class);
        when(repo.search(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        var adapter = adapterWith(repo);

        SearchQuery q = SearchQuery.builder(
                "latte", "coffee",
                350, 900, // explicit bounds -> forwarded
                SortRule.PRICE_HIGH_TO_LOW,
                List.of() // empty list -> no JSON
        );

        adapter.search(q, PageRequest.of(1, 5));

        ArgumentCaptor<Integer> minCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> maxCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> attrCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sortCap = ArgumentCaptor.forClass(String.class);

        verify(repo).search(
                any(), any(),
                minCap.capture(),
                maxCap.capture(),
                sortCap.capture(),
                any(), attrCap.capture(), any()
        );

        assertEquals(350, minCap.getValue()); // kept
        assertEquals(900, maxCap.getValue()); // kept
        assertNull(attrCap.getValue()); // no attributes -> null JSON
        assertEquals("PRICE_HIGH_TO_LOW", sortCap.getValue());
    }

    /**
     * With null attribute list and DEFAULT sort:
     * - attrJson remains null.
     * - min=0 and max=Integer.MAX_VALUE normalize to nulls.
     * - Blank key and null category are forwarded unchanged (domain normalizes inputs already).
     */
    @Test
    @DisplayName("null attrs + DEFAULT sort -> attr null; defaults normalized")
    void nullAttrs_defaultSort_defaultsNormalized() {
        ProductJpaRepository repo = mock(ProductJpaRepository.class);
        when(repo.search(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        var adapter = adapterWith(repo);

        SearchQuery q = SearchQuery.builder(
                "", // blank key -> forwarded
                null, // null category -> forwarded
                0, // -> null
                Integer.MAX_VALUE, // -> null
                SortRule.DEFAULT,
                null // null attributes -> attrJson null
        );

        adapter.search(q, PageRequest.of(0, 10));

        ArgumentCaptor<String> catCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> minCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> maxCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> sortCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> attrCap = ArgumentCaptor.forClass(String.class);

        verify(repo).search(
                catCap.capture(),
                keyCap.capture(),
                minCap.capture(),
                maxCap.capture(),
                sortCap.capture(),
                any(), attrCap.capture(), any()
        );

        assertNull(catCap.getValue()); // forwarded
        assertEquals("", keyCap.getValue()); // forwarded
        assertNull(minCap.getValue()); // 0 -> null
        assertNull(maxCap.getValue()); // MAX -> null
        assertEquals("DEFAULT", sortCap.getValue()); // enum name
        assertNull(attrCap.getValue()); // no JSON
    }
}
