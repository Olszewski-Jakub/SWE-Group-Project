package ie.universityofgalway.groupnine.infrastructure.payments.adapter;

import com.stripe.model.checkout.Session;
import ie.universityofgalway.groupnine.domain.payment.IdempotencyKey;
import ie.universityofgalway.groupnine.infrastructure.payments.builder.CheckoutParamsBuilder;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutLineItem;
import ie.universityofgalway.groupnine.service.payments.port.dto.ShippingOptions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class StripePaymentGatewayAdapterTest {

    @Test
    void toResult_maps_id_url_and_payment_intent() {
        CheckoutParamsBuilder params = new CheckoutParamsBuilder();
        StripePaymentGatewayAdapter adapter = new StripePaymentGatewayAdapter(params);

        Session s = Mockito.mock(Session.class);
        when(s.getId()).thenReturn("cs_test_123");
        when(s.getUrl()).thenReturn("https://checkout.stripe.com/s/cs_test_123");
        when(s.getPaymentIntent()).thenReturn("pi_abc");

        var result = adapter.toResult(s);
        assertEquals("cs_test_123", result.getSessionId());
        assertEquals("https://checkout.stripe.com/s/cs_test_123", result.getUrl());
        assertEquals("pi_abc", result.getPaymentIntentId());
    }

    @Test
    void toResult_handles_missing_payment_intent() {
        CheckoutParamsBuilder params = new CheckoutParamsBuilder();
        StripePaymentGatewayAdapter adapter = new StripePaymentGatewayAdapter(params);

        Session s = Mockito.mock(Session.class);
        when(s.getId()).thenReturn("cs_test_999");
        when(s.getUrl()).thenReturn("u");
        Mockito.doThrow(new RuntimeException("no pi")).when(s).getPaymentIntent();

        var result = adapter.toResult(s);
        assertNull(result.getPaymentIntentId());
    }
}

