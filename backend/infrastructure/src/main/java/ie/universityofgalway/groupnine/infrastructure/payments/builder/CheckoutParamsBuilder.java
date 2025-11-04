package ie.universityofgalway.groupnine.infrastructure.payments.builder;

import com.stripe.param.checkout.SessionCreateParams;
import ie.universityofgalway.groupnine.domain.payment.PaymentMetadata;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutLineItem;
import ie.universityofgalway.groupnine.service.payments.port.dto.ShippingOptions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Helper that constructs Stripe {@link com.stripe.param.checkout.SessionCreateParams} from
 * gateway‑agnostic inputs (line items, metadata, URLs, and shipping options). Encapsulates
 * UI and collection preferences for a consistent Checkout experience.
 */
@Component
public class CheckoutParamsBuilder {

    /** Builds the {@link SessionCreateParams} for a new Checkout Session. */
    public SessionCreateParams build(
            List<CheckoutLineItem> items,
            String currency,
            PaymentMetadata metadata,
            String successUrl,
            String cancelUrl,
            ShippingOptions shippingOptions
    ) {
        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setUiMode(SessionCreateParams.UiMode.HOSTED)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                .setPhoneNumberCollection(SessionCreateParams.PhoneNumberCollection.builder().setEnabled(true).build());

        addMetadata(sessionBuilder, metadata);
        addLineItems(sessionBuilder, items, currency);
        configureShipping(sessionBuilder, shippingOptions, currency);

        return sessionBuilder.build();
    }

    /** Copies metadata to both the session and the underlying payment intent. */
    private void addMetadata(SessionCreateParams.Builder builder, PaymentMetadata metadata) {
        if (metadata == null || metadata.getMetadata().isEmpty()) return;
        builder.putAllMetadata(metadata.getMetadata());
        SessionCreateParams.PaymentIntentData.Builder pid = SessionCreateParams.PaymentIntentData.builder();
        metadata.getMetadata().forEach(pid::putMetadata);
        builder.setPaymentIntentData(pid.build());
    }

    /** Adds line items with per‑unit amounts and the given currency. */
    private void addLineItems(SessionCreateParams.Builder builder, List<CheckoutLineItem> items, String currency) {
        for (CheckoutLineItem li : items) {
            SessionCreateParams.LineItem.PriceData.ProductData prod =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(li.getName()).build();
            SessionCreateParams.LineItem.PriceData price =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(currency.toLowerCase())
                            .setUnitAmount(li.getUnitAmountMinor())
                            .setProductData(prod)
                            .build();
            SessionCreateParams.LineItem item = SessionCreateParams.LineItem.builder()
                    .setQuantity((long) li.getQuantity())
                    .setPriceData(price)
                    .build();
            builder.addLineItem(item);
        }
    }

    /** Applies shipping address collection and predefined shipping rates, when provided. */
    private void configureShipping(SessionCreateParams.Builder builder, ShippingOptions shippingOptions, String currency) {
        List<String> countries = shippingOptions.getAllowedCountries();
        if (!countries.isEmpty()) {
            SessionCreateParams.ShippingAddressCollection.Builder sab = SessionCreateParams.ShippingAddressCollection.builder();
            for (String code : countries) {
                try {
                    var ec = SessionCreateParams.ShippingAddressCollection.AllowedCountry.valueOf(code.toUpperCase());
                    sab.addAllowedCountry(ec);
                } catch (Exception ignored) {
                }
            }
            builder.setShippingAddressCollection(sab.build());
            builder.setCurrency(currency);
        }
        List<String> rates = shippingOptions.getShippingRateIds();
        if (!rates.isEmpty()) {
            for (String rateId : rates) {
                builder.addShippingOption(SessionCreateParams.ShippingOption.builder().setShippingRate(rateId).build());
            }
        }
    }
}
