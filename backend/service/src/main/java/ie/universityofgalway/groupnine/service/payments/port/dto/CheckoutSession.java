package ie.universityofgalway.groupnine.service.payments.port.dto;

/**
 * Result returned by {@link ie.universityofgalway.groupnine.service.payments.port.PaymentGatewayPort}
 * after creating a hosted Checkout session with an external provider.
 * <p>
 * The type only exposes provider‑agnostic identifiers and the public URL for redirecting
 * customers to complete their payment.
 */
public final class CheckoutSession {
    private final String sessionId;
    private final String url;
    private final String paymentIntentId;

    /**
     * Creates a new immutable session descriptor.
     *
     * @param sessionId       gateway‑specific session identifier
     * @param url             customer‑facing URL to the hosted payment page
     * @param paymentIntentId optional payment intent identifier (may be null depending on provider)
     */
    public CheckoutSession(String sessionId, String url, String paymentIntentId) {
        this.sessionId = sessionId;
        this.url = url;
        this.paymentIntentId = paymentIntentId;
    }

    /** @return gateway‑specific session identifier */
    public String getSessionId() { return sessionId; }
    /** @return customer‑facing URL to the hosted payment page */
    public String getUrl() { return url; }
    /** @return optional payment intent identifier; may be null */
    public String getPaymentIntentId() { return paymentIntentId; }
}
