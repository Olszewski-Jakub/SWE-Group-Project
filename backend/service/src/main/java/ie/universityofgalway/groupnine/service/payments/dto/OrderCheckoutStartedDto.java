package ie.universityofgalway.groupnine.service.payments.dto;

public final class OrderCheckoutStartedDto {
    public final String order_id;
    public final String session_id;
    public final String payment_intent_id;
    public final long total_minor;
    public final String currency;

    public OrderCheckoutStartedDto(String orderId, String sessionId, String paymentIntentId, long totalMinor, String currency) {
        this.order_id = orderId;
        this.session_id = sessionId;
        this.payment_intent_id = paymentIntentId;
        this.total_minor = totalMinor;
        this.currency = currency;
    }
}