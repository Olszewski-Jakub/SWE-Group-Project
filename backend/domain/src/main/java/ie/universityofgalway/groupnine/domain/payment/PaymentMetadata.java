package ie.universityofgalway.groupnine.domain.payment;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.user.UserId;

import java.util.HashMap;
import java.util.Map;

public class PaymentMetadata {

    private final Map<String, String> metadata;

    private PaymentMetadata(Builder builder) {
        this.metadata = Map.copyOf(builder.metadata);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String get(String key) {
        return metadata.get(key);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> metadata = new HashMap<>();

        public Builder orderId(OrderId orderId) {
            metadata.put("order_id", orderId.toString());
            return this;
        }

        public Builder cartId(CartId cartId) {
            metadata.put("cart_id", cartId.toString());
            return this;
        }

        public Builder userId(UserId userId) {
            metadata.put("user_id", String.valueOf(userId));
            return this;
        }

        public Builder put(String key, String value) {
            metadata.put(key, value);
            return this;
        }

        public PaymentMetadata build() {
            return new PaymentMetadata(this);
        }
    }

    @Override
    public String toString() {
        return "PaymentMetadata" + metadata;
    }
}
