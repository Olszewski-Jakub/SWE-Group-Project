package ie.universityofgalway.groupnine.infrastructure.payments.builder;

import com.stripe.param.checkout.SessionCreateParams;
import ie.universityofgalway.groupnine.domain.payment.PaymentMetadata;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutLineItem;
import ie.universityofgalway.groupnine.service.payments.port.dto.ShippingOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutParamsBuilderTest {

    @Test
    void build_sets_core_fields_line_items_and_metadata() {
        CheckoutParamsBuilder b = new CheckoutParamsBuilder();

        List<CheckoutLineItem> items = List.of(
                new CheckoutLineItem("Item A", 123, 2),
                new CheckoutLineItem("Item B", 999, 1)
        );
        PaymentMetadata md = PaymentMetadata.builder().put("order_id", "o-1").put("cart_id", "c-1").build();
        ShippingOptions shipping = new ShippingOptions(List.of("US", "IE"), List.of());

        SessionCreateParams p = b.build(items, "EUR", md, "https://ok/", "https://ko/", shipping);

        assertEquals(SessionCreateParams.Mode.PAYMENT, p.getMode());
        assertEquals(SessionCreateParams.UiMode.HOSTED, p.getUiMode());
        assertEquals("https://ok/", p.getSuccessUrl());
        assertEquals("https://ko/", p.getCancelUrl());
        assertNotNull(p.getBillingAddressCollection());
        assertTrue(p.getPhoneNumberCollection().getEnabled());

        // Line items mapped
        assertEquals(2, p.getLineItems().size());
        assertEquals(2L, p.getLineItems().get(0).getQuantity());
        assertEquals(123L, p.getLineItems().get(0).getPriceData().getUnitAmount());
        assertEquals("eur", p.getLineItems().get(0).getPriceData().getCurrency());
        assertEquals("Item A", p.getLineItems().get(0).getPriceData().getProductData().getName());

        // Metadata propagated
        assertEquals("o-1", p.getMetadata().get("order_id"));
        assertNotNull(p.getPaymentIntentData());
        assertEquals("c-1", p.getPaymentIntentData().getMetadata().get("cart_id"));

        // Shipping address collection applied (allowed countries set)
        assertNotNull(p.getShippingAddressCollection());
        assertTrue(p.getShippingAddressCollection().getAllowedCountries().size() >= 2);
    }

    @Test
    void build_adds_shipping_rate_ids_when_provided() {
        CheckoutParamsBuilder b = new CheckoutParamsBuilder();
        List<CheckoutLineItem> items = List.of(new CheckoutLineItem("X", 1, 1));
        ShippingOptions shipping = new ShippingOptions(List.of(), List.of("shr_123", "shr_456"));

        SessionCreateParams p = b.build(items, "USD", null, "a", "b", shipping);
        assertEquals(2, p.getShippingOptions().size());
        assertEquals("shr_123", p.getShippingOptions().get(0).getShippingRate());
    }
}
