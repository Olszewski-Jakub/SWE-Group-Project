package ie.universityofgalway.groupnine.delivery.rest.checkout.dto;

/**
 * Response body returned after creating a new hosted Checkout session. It includes
 * the newly created Order id, the session id from the payment provider, and a URL
 * to redirect the customer.
 */
public final class CheckoutSessionResponse {
    public final String orderId;
    public final String sessionId;
    public final String checkoutUrl;

    public CheckoutSessionResponse(String orderId, String sessionId, String checkoutUrl) {
        this.orderId = orderId;
        this.sessionId = sessionId;
        this.checkoutUrl = checkoutUrl;
    }
}
