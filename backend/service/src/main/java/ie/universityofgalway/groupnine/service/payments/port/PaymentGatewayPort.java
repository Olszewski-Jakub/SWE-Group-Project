package ie.universityofgalway.groupnine.service.payments.port;

import ie.universityofgalway.groupnine.domain.payment.IdempotencyKey;
import ie.universityofgalway.groupnine.domain.payment.PaymentMetadata;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutLineItem;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutSession;
import ie.universityofgalway.groupnine.service.payments.port.dto.ShippingOptions;

import java.util.List;
import java.util.Map;

/**
 * Outbound port for payment gateways (Stripe) to create Checkout Sessions.
 */
public interface PaymentGatewayPort {
    CheckoutSession createCheckoutSession(
            List<CheckoutLineItem> items,
            String currency,
            PaymentMetadata metadata,
            String successUrl,
            String cancelUrl,
            IdempotencyKey idempotencyKey,
            ShippingOptions shippingOptions
    );
}
