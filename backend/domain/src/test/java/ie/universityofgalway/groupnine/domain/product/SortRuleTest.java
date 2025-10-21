package ie.universityofgalway.groupnine.domain.product;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SortRuleTest {
    @Test
    void parse_matchesEnumCaseInsensitively_andDefaults() {
        assertEquals(SortRule.DEFAULT, SortRule.parse(null));
        assertEquals(SortRule.DEFAULT, SortRule.parse(" "));
        assertEquals(SortRule.PRICE_LOW_TO_HIGH, SortRule.parse("price_low_to_high"));
        assertEquals(SortRule.PRICE_HIGH_TO_LOW, SortRule.parse("Price_High_To_Low"));
        assertEquals(SortRule.NEWEST_FIRST, SortRule.parse("newest_first"));
        assertEquals(SortRule.DEFAULT, SortRule.parse("unknown"));
    }
}

