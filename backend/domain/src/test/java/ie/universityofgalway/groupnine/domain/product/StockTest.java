package ie.universityofgalway.groupnine.domain.product;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {
    @Test
    void available_calculatesQuantityMinusReserved() {
        Stock s = new Stock(10, 3);
        assertEquals(7, s.available());
        s = new Stock(5, 0);
        assertEquals(5, s.available());
    }
}

