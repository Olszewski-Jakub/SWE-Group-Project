package ie.universityofgalway.groupnine.service.payments.webhook.port;

import ie.universityofgalway.groupnine.domain.payment.EventEnvelope;

/**
 * Port responsible for verifying the Stripe webhook signature and parsing the raw
 * JSON payload into a normalized {@link EventEnvelope}. Implementations should be
 * conservative and treat unknown or malformed payloads as invalid.
 */
public interface StripeEventParserPort {
    /**
     * Verifies the signature and converts the raw payload into an {@link EventEnvelope}.
     *
     * @param payload   raw request body received from Stripe
     * @param signature contents of the {@code Stripe-Signature} header
     * @return a normalized envelope with a stable event id, type and flattened payload
     * @throws InvalidSignatureException when the signature is missing/invalid or parsing fails
     */
    EventEnvelope parse(String payload, String signature) throws InvalidSignatureException;

    /** Exception that indicates signature verification or parsing failure. */
    class InvalidSignatureException extends Exception {
        public InvalidSignatureException(String message, Throwable cause) { super(message, cause); }
        public InvalidSignatureException(String message) { super(message); }
    }
}
