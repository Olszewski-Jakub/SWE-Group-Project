package ie.universityofgalway.groupnine.service.payments.dto;

import ie.universityofgalway.groupnine.domain.order.OrderId;

public final class StartCheckoutResultDto {
    private final OrderId orderId;
    private final String sessionId;
    private final String checkoutUrl;

    public StartCheckoutResultDto(OrderId orderId, String sessionId, String checkoutUrl) {
        this.orderId = orderId;
        this.sessionId = sessionId;
        this.checkoutUrl = checkoutUrl;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }
}
