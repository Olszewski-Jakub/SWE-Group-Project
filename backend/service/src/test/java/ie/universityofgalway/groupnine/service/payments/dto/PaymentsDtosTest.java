package ie.universityofgalway.groupnine.service.payments.dto;

import ie.universityofgalway.groupnine.domain.payment.OrderSnapshotItem;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutLineItem;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutSession;
import ie.universityofgalway.groupnine.service.payments.port.dto.ShippingOptions;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentsDtosTest {

    @Test
    void inventoryReserveRequest_maps_items() {
        OrderSnapshotItem it = new OrderSnapshotItem(new VariantId(UUID.randomUUID()), "SKU", 123, 2, Currency.getInstance("EUR"));
        InventoryReserveRequestDto dto = new InventoryReserveRequestDto("o1", List.of(it), "2024-01-01T00:00:00Z");
        assertEquals("o1", dto.order_id);
        assertEquals(1, dto.items.size());
        assertEquals(2, dto.items.get(0).quantity);
        assertNotNull(dto.items.get(0).variant_id);
    }

    @Test
    void orderCheckoutStarted_and_startCheckoutResult_hold_values() {
        OrderCheckoutStartedDto evt = new OrderCheckoutStartedDto("o1", "cs", "pi", 999, "EUR");
        assertEquals("o1", evt.order_id);
        assertEquals("cs", evt.session_id);
        assertEquals("pi", evt.payment_intent_id);
        assertEquals(999, evt.total_minor);
        assertEquals("EUR", evt.currency);

        var result = new ie.universityofgalway.groupnine.service.payments.dto.StartCheckoutResultDto(
                ie.universityofgalway.groupnine.domain.order.OrderId.of(UUID.randomUUID()),
                "cs_1",
                "https://checkout"
        );
        assertNotNull(result.getOrderId());
        assertEquals("cs_1", result.getSessionId());
        assertEquals("https://checkout", result.getCheckoutUrl());
    }

    @Test
    void portDtos_hold_values_and_immutability() {
        CheckoutLineItem li = new CheckoutLineItem("Coffee", 123, 2);
        assertEquals("Coffee", li.getName());
        assertEquals(123, li.getUnitAmountMinor());
        assertEquals(2, li.getQuantity());

        CheckoutSession cs = new CheckoutSession("cs", "url", "pi");
        assertEquals("cs", cs.getSessionId());
        assertEquals("url", cs.getUrl());
        assertEquals("pi", cs.getPaymentIntentId());

        ShippingOptions so = new ShippingOptions(List.of("IE"), List.of("shr_1"));
        assertEquals(List.of("IE"), so.getAllowedCountries());
        assertEquals(List.of("shr_1"), so.getShippingRateIds());
    }
}

