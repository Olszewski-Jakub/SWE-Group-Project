package ie.universityofgalway.groupnine.infrastructure.payments.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import ie.universityofgalway.groupnine.domain.payment.EventEnvelope;
import ie.universityofgalway.groupnine.service.payments.webhook.port.StripeEventParserPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe implementation of {@link ie.universityofgalway.groupnine.service.payments.webhook.port.StripeEventParserPort}.
 * Verifies webhook signatures and normalizes Stripe objects (Checkout Session, PaymentIntent) into
 * a flattened {@link ie.universityofgalway.groupnine.domain.payment.EventEnvelope} map used by workers.
 */
@Component
public class StripeEventParserAdapter implements StripeEventParserPort {
    private final String webhookSecret;
    private final ObjectMapper om = new ObjectMapper();

    public StripeEventParserAdapter(@Value("${stripe.webhook-secret:}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    @Override
    public EventEnvelope parse(String payload, String signature) throws InvalidSignatureException {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new InvalidSignatureException("STRIPE_WEBHOOK_SECRET not configured");
        }
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            String eventId = event.getId();
            String type = event.getType();
            Instant occurred = event.getCreated() == null ? Instant.now() : Instant.ofEpochSecond(event.getCreated());

            Map<String, Object> normalized = new HashMap<>();
            normalized.put("stripe_event_id", eventId);
            normalized.put("type", type);
            normalized.put("occurred_at", occurred.toString());

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer != null && deserializer.getObject().isPresent()) {
                StripeObject obj = deserializer.getObject().get();
                enrichFromStripeObject(obj, normalized);
            } else if (deserializer != null) {
                String raw = deserializer.getRawJson();
                if (raw != null) enrichFromRaw(raw, normalized);
            }
            return new EventEnvelope(eventId, type, occurred, normalized);
        } catch (SignatureVerificationException e) {
            throw new InvalidSignatureException("invalid signature", e);
        } catch (Exception e) {
            // Treat unknown parse errors as invalid for security
            throw new InvalidSignatureException("parse_error", e);
        }
    }

    private void enrichFromStripeObject(StripeObject obj, Map<String, Object> normalized) {
        if (obj instanceof Session s) {
            Map<String, String> md = s.getMetadata();
            if (md != null) normalized.putAll(md);
            try { if (s.getAmountTotal() != null) normalized.put("amount_minor", s.getAmountTotal()); } catch (Throwable ignored) {}
            if (s.getCurrency() != null) normalized.put("currency", s.getCurrency());
            try {
                if (s.getShippingCost() != null) {
                    var sc = s.getShippingCost();
                    if (sc.getShippingRate() != null) normalized.put("shipping_rate_id", sc.getShippingRate());
                    if (sc.getAmountTotal() != null) normalized.put("shipping_amount_minor", sc.getAmountTotal());
                    if (s.getCurrency() != null) normalized.put("shipping_currency", s.getCurrency());
                }
                if (s.getShippingDetails() != null && s.getShippingDetails().getAddress() != null) {
                    var sh = s.getShippingDetails();
                    var addr = sh.getAddress();
                    normalized.put("shipping_name", sh.getName());
                    normalized.put("shipping_phone", sh.getPhone());
                    normalized.put("shipping_address_line1", addr.getLine1());
                    normalized.put("shipping_address_line2", addr.getLine2());
                    normalized.put("shipping_city", addr.getCity());
                    normalized.put("shipping_state", addr.getState());
                    normalized.put("shipping_postal_code", addr.getPostalCode());
                    normalized.put("shipping_country", addr.getCountry());
                } else if (s.getCustomerDetails() != null && s.getCustomerDetails().getAddress() != null) {
                    // Fallback to customer_details address if shipping_details is absent
                    var cd = s.getCustomerDetails();
                    var addr = cd.getAddress();
                    normalized.put("shipping_name", cd.getName());
                    normalized.put("shipping_phone", cd.getPhone());
                    normalized.put("shipping_address_line1", addr.getLine1());
                    normalized.put("shipping_address_line2", addr.getLine2());
                    normalized.put("shipping_city", addr.getCity());
                    normalized.put("shipping_state", addr.getState());
                    normalized.put("shipping_postal_code", addr.getPostalCode());
                    normalized.put("shipping_country", addr.getCountry());
                }
            } catch (Throwable ignored) {}
        }
        if (obj instanceof com.stripe.model.PaymentIntent pi) {
            if (pi.getAmount() != null) normalized.put("amount_minor", pi.getAmount());
            if (pi.getCurrency() != null) normalized.put("currency", pi.getCurrency());
            Map<String, String> md = pi.getMetadata();
            if (md != null) normalized.putAll(md);
            try {
                if (pi.getShipping() != null && pi.getShipping().getAddress() != null) {
                    var sh = pi.getShipping();
                    var addr = sh.getAddress();
                    normalized.put("shipping_name", sh.getName());
                    normalized.put("shipping_phone", sh.getPhone());
                    normalized.put("shipping_address_line1", addr.getLine1());
                    normalized.put("shipping_address_line2", addr.getLine2());
                    normalized.put("shipping_city", addr.getCity());
                    normalized.put("shipping_state", addr.getState());
                    normalized.put("shipping_postal_code", addr.getPostalCode());
                    normalized.put("shipping_country", addr.getCountry());
                }
            } catch (Throwable ignored) {}
        }
    }

    private void enrichFromRaw(String raw, Map<String, Object> normalized) {
        try {
            JsonNode node = om.readTree(raw);
            if (node.has("metadata")) node.get("metadata").fields().forEachRemaining(e -> normalized.put(e.getKey(), e.getValue().asText()));
            if (node.has("amount_total")) normalized.put("amount_minor", node.get("amount_total").asLong());
            if (node.has("currency")) normalized.put("currency", node.get("currency").asText());
            if (node.has("shipping_cost") && node.get("shipping_cost").isObject()) {
                var sc = node.get("shipping_cost");
                if (sc.has("shipping_rate")) normalized.put("shipping_rate_id", sc.get("shipping_rate").asText());
                if (sc.has("amount_total")) normalized.put("shipping_amount_minor", sc.get("amount_total").asLong());
            }
            if (node.has("shipping_details") && node.get("shipping_details").isObject()) {
                var sh = node.get("shipping_details");
                if (sh.has("name")) normalized.put("shipping_name", sh.get("name").asText());
                if (sh.has("phone")) normalized.put("shipping_phone", sh.get("phone").asText());
                if (sh.has("address") && sh.get("address").isObject()) {
                    var ad = sh.get("address");
                    if (ad.has("line1")) normalized.put("shipping_address_line1", ad.get("line1").asText());
                    if (ad.has("line2")) normalized.put("shipping_address_line2", ad.get("line2").asText());
                    if (ad.has("city")) normalized.put("shipping_city", ad.get("city").asText());
                    if (ad.has("state")) normalized.put("shipping_state", ad.get("state").asText());
                    if (ad.has("postal_code")) normalized.put("shipping_postal_code", ad.get("postal_code").asText());
                    if (ad.has("country")) normalized.put("shipping_country", ad.get("country").asText());
                }
            } else if (node.has("customer_details") && node.get("customer_details").isObject()) {
                var cd = node.get("customer_details");
                if (cd.has("name")) normalized.put("shipping_name", cd.get("name").asText());
                if (cd.has("phone")) normalized.put("shipping_phone", cd.get("phone").asText());
                if (cd.has("address") && cd.get("address").isObject()) {
                    var ad = cd.get("address");
                    if (ad.has("line1")) normalized.put("shipping_address_line1", ad.get("line1").asText());
                    if (ad.has("line2")) normalized.put("shipping_address_line2", ad.get("line2").asText());
                    if (ad.has("city")) normalized.put("shipping_city", ad.get("city").asText());
                    if (ad.has("state")) normalized.put("shipping_state", ad.get("state").asText());
                    if (ad.has("postal_code")) normalized.put("shipping_postal_code", ad.get("postal_code").asText());
                    if (ad.has("country")) normalized.put("shipping_country", ad.get("country").asText());
                }
            }
            if (node.has("amount")) normalized.put("amount_minor", node.get("amount").asLong());
            if (node.has("shipping") && node.get("shipping").isObject()) {
                var sh = node.get("shipping");
                if (sh.has("name")) normalized.put("shipping_name", sh.get("name").asText());
                if (sh.has("phone")) normalized.put("shipping_phone", sh.get("phone").asText());
                if (sh.has("address") && sh.get("address").isObject()) {
                    var ad = sh.get("address");
                    if (ad.has("line1")) normalized.put("shipping_address_line1", ad.get("line1").asText());
                    if (ad.has("line2")) normalized.put("shipping_address_line2", ad.get("line2").asText());
                    if (ad.has("city")) normalized.put("shipping_city", ad.get("city").asText());
                    if (ad.has("state")) normalized.put("shipping_state", ad.get("state").asText());
                    if (ad.has("postal_code")) normalized.put("shipping_postal_code", ad.get("postal_code").asText());
                    if (ad.has("country")) normalized.put("shipping_country", ad.get("country").asText());
                }
            }
        } catch (Exception ignored) {}
    }
}
