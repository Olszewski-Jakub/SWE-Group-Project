package ie.universityofgalway.groupnine.domain.payment;

import ie.universityofgalway.groupnine.domain.order.OrderId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IdempotencyKey {
    private final String idemKey;


    private IdempotencyKey(OrderId orderId, OrderSnapshot snapshot) {
        this.idemKey = idempotencyKey(orderId, snapshot);
    }

    public static IdempotencyKey from(OrderId orderId, OrderSnapshot snapshot) {
        return new IdempotencyKey(orderId, snapshot);
    }

    public String getIdemKey() {
        return idemKey;
    }

    private String idempotencyKey(OrderId orderId, OrderSnapshot snapshot) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(orderId.toString().getBytes(StandardCharsets.UTF_8));
            md.update(Long.toString(snapshot.getTotalMinor()).getBytes(StandardCharsets.UTF_8));
            md.update(snapshot.getCurrency().getCurrencyCode().getBytes(StandardCharsets.UTF_8));
            for (OrderSnapshotItem it : snapshot.getItems()) {
                md.update(it.getVariantId().toString().getBytes(StandardCharsets.UTF_8));
                md.update(it.getSku().getBytes(StandardCharsets.UTF_8));
                md.update(Long.toString(it.getUnitAmountMinor()).getBytes(StandardCharsets.UTF_8));
                md.update(Integer.toString(it.getQuantity()).getBytes(StandardCharsets.UTF_8));
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return orderId + ":" + sb;
        } catch (NoSuchAlgorithmException e) {
            return orderId + ":fallback";
        }
    }
}
