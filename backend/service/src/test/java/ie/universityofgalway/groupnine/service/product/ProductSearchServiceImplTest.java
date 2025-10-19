package ie.universityofgalway.groupnine.service.product;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.SearchQuery;
import ie.universityofgalway.groupnine.domain.product.SortRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Minimal unit tests for ProductSearchServiceImpl.
 *
 * Focus:
 * - Service validation (null, negative, min > max).
 * - Sort mapping (DEFAULT, price sorts, NEWEST_FIRST, null sort).
 * - Unexpected sort value triggers IllegalStateException.
 * - Delegation to ProductPort with correct Pageable.
 *
 * Note: Domain classes (SearchQuery) normalize many invalid inputs; these tests
 * still verify the service’s own guards and sort behavior.
 */
class ProductSearchServiceImplTest {

    // ---------- Validation ----------

    /** Null request must be rejected by the service. */
    @Test
    @DisplayName("null request -> IllegalArgumentException")
    void nullRequest_throws() {
        ProductPort port = mock(ProductPort.class);
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(port);

        assertThrows(IllegalArgumentException.class, () -> svc.search(null, 0, 10));
    }

    /** Negative prices are rejected (service guard), even if domain normally normalizes. */
    @Test
    @DisplayName("negative prices -> IllegalArgumentException")
    void negativePrices_throws() {
        ProductPort port = mock(ProductPort.class);
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(port);

        SearchQuery q = mock(SearchQuery.class);
        when(q.minPriceCents()).thenReturn(-1);
        when(q.maxPriceCents()).thenReturn(0);
        when(q.sortRule()).thenReturn(SortRule.DEFAULT);

        assertThrows(IllegalArgumentException.class, () -> svc.search(q, 0, 10));
    }

    /** min > max is rejected (service guard), even if domain normally coerces max to min. */
    @Test
    @DisplayName("min > max -> IllegalArgumentException")
    void minGreaterThanMax_throws() {
        ProductPort port = mock(ProductPort.class);
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(port);

        SearchQuery q = mock(SearchQuery.class);
        when(q.minPriceCents()).thenReturn(200);
        when(q.maxPriceCents()).thenReturn(100);
        when(q.sortRule()).thenReturn(SortRule.DEFAULT);

        assertThrows(IllegalArgumentException.class, () -> svc.search(q, 0, 10));
    }

    // ---------- Sort mapping ----------

    /** DEFAULT should produce an unsorted pageable. */
    @Test
    @DisplayName("DEFAULT -> unsorted pageable")
    void defaultSort_isUnsorted() {
        ProductPort port = mock(ProductPort.class);
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(port);

        SearchQuery q = SearchQuery.builder("", null, 0, 0, SortRule.DEFAULT, List.of());
        when(port.search(any(), any(Pageable.class))).thenReturn(new PageImpl<Product>(List.of()));

        svc.search(q, 0, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(port).search(eq(q), captor.capture());
        assertFalse(captor.getValue().getSort().isSorted());
    }

    /** Price-based sorts are handled in the repository layer; pageable remains unsorted. */
    @Test
    @DisplayName("PRICE sorts -> unsorted pageable")
    void priceSorts_areUnsorted() {
        ProductPort port = mock(ProductPort.class);
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(port);

        for (SortRule r : new SortRule[]{SortRule.PRICE_LOW_TO_HIGH, SortRule.PRICE_HIGH_TO_LOW}) {
            SearchQuery q = SearchQuery.builder("", null, 0, 0, r, List.of());
            when(port.search(any(), any(Pageable.class))).thenReturn(new PageImpl<Product>(List.of()));

            svc.search(q, 0, 5);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(port, atLeastOnce()).search(eq(q), captor.capture());
            assertFalse(captor.getValue().getSort().isSorted());
        }
    }

    /** NEWEST_FIRST should sort by createdAt DESC. */
    @Test
    @DisplayName("NEWEST_FIRST -> sort by createdAt DESC")
    void newestFirst_sortsByCreatedAtDesc() {
        ProductPort port = mock(ProductPort.class);
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(port);

        SearchQuery q = SearchQuery.builder("", null, 0, 0, SortRule.NEWEST_FIRST, List.of());
        when(port.search(any(), any(Pageable.class))).thenReturn(new PageImpl<Product>(List.of()));

        svc.search(q, 0, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(port).search(eq(q), captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("createdAt");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    // ---------- Null and unexpected sort ----------

    /** Null sort is treated as unsorted by the service. */
    @Test
    @DisplayName("null sort -> unsorted pageable")
    void nullSort_isUnsorted() {
        ProductPort port = mock(ProductPort.class);
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(port);

        // Start from a normalized query; force sortRule() to null with a spy.
        SearchQuery base = SearchQuery.builder("", null, 0, 0, SortRule.DEFAULT, List.of());
        SearchQuery q = spy(base);
        when(q.sortRule()).thenReturn(null);

        when(port.search(any(), any(Pageable.class))).thenReturn(new PageImpl<Product>(List.of()));

        svc.search(q, 0, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(port).search(eq(q), captor.capture());
        assertFalse(captor.getValue().getSort().isSorted());
    }

    /** An unexpected sort value should hit the switch default and throw IllegalStateException. */
    @Test
    @DisplayName("unexpected sort value -> IllegalStateException")
    void unexpectedSort_throwsIllegalState() {
        ProductPort port = mock(ProductPort.class);
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(port);

        // Mock a SortRule that doesn't match any known enum constant.
        SortRule unknown = mock(SortRule.class);

        SearchQuery q = mock(SearchQuery.class);
        when(q.minPriceCents()).thenReturn(0);
        when(q.maxPriceCents()).thenReturn(0);
        when(q.sortRule()).thenReturn(unknown);

        assertThrows(IllegalStateException.class, () -> svc.search(q, 0, 5));
    }

    // ---------- Delegation ----------

    /** Service should delegate to ProductPort and return its page. */
    @Test
    @DisplayName("delegates to ProductPort and returns result")
    void delegates_andReturnsPortPage() {
        ProductPort port = mock(ProductPort.class);
        ProductSearchServiceImpl svc = new ProductSearchServiceImpl(port);

        SearchQuery q = SearchQuery.builder("coffee", null, 0, 0, SortRule.DEFAULT, List.of());
        Page<Product> expected = new PageImpl<>(List.of());
        when(port.search(eq(q), any(Pageable.class))).thenReturn(expected);

        Page<Product> actual = svc.search(q, 0, 10);

        assertSame(expected, actual);
        verify(port).search(eq(q), any(Pageable.class));
    }
}
