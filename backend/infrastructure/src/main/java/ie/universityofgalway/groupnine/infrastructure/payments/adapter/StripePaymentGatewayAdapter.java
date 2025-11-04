package ie.universityofgalway.groupnine.infrastructure.payments.adapter;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import ie.universityofgalway.groupnine.domain.payment.IdempotencyKey;
import ie.universityofgalway.groupnine.domain.payment.PaymentMetadata;
import ie.universityofgalway.groupnine.infrastructure.payments.builder.CheckoutParamsBuilder;
import ie.universityofgalway.groupnine.service.payments.port.PaymentGatewayPort;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutLineItem;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutSession;
import ie.universityofgalway.groupnine.service.payments.port.dto.ShippingOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stripe-based implementation of PaymentGatewayPort for creating Checkout Sessions.
 */
/**
 * Stripe implementation of {@link ie.universityofgalway.groupnine.service.payments.port.PaymentGatewayPort}
 * for creating hosted Checkout sessions via the Stripe Java SDK.
 */
@Component
public class StripePaymentGatewayAdapter implements PaymentGatewayPort {
    private static final Logger LOG = LoggerFactory.getLogger(StripePaymentGatewayAdapter.class);
    private final CheckoutParamsBuilder paramsBuilder;

    public StripePaymentGatewayAdapter(CheckoutParamsBuilder paramsBuilder) {
        this.paramsBuilder = paramsBuilder;
    }


    /**
     * Creates a Checkout Session using the Stripe SDK and returns a provider‑agnostic result.
     */
    @Override
    public CheckoutSession createCheckoutSession(
            List<CheckoutLineItem> items,
            String currency,
            PaymentMetadata metadata,
            String successUrl,
            String cancelUrl,
            IdempotencyKey idempotencyKey,
            ShippingOptions shippingOptions
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(successUrl, "successUrl");
        Objects.requireNonNull(cancelUrl, "cancelUrl");

        SessionCreateParams params = paramsBuilder.build(items, currency, metadata, successUrl, cancelUrl, shippingOptions);
        RequestOptions opts = RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey.getIdemKey())
                .build();

        try {
            Session session = Session.create(params, opts);
            return toResult(session);
        } catch (StripeException e) {
            throw new IllegalStateException("stripe_session_create_failed: " + e.getMessage(), e);
        }
    }

    /** Maps a Stripe {@link Session} to the portable {@link CheckoutSession} type. */
    protected CheckoutSession toResult(Session s) {
        String id = s.getId();
        String url = s.getUrl();
        String paymentIntentId;
        try {
            paymentIntentId = s.getPaymentIntent();
        } catch (Throwable t) {
            paymentIntentId = null;
        }
        return new CheckoutSession(id, url, paymentIntentId);
    }
}
