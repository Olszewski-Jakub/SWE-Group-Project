package ie.universityofgalway.groupnine.domain.messaging;

import java.time.Instant;
import java.util.UUID;

public class OutboxMessage {

    private UUID id;

    private String exchange;

    private String routingKey;

    private String headersJson;

    private String payloadJson;

    private Instant createdAt;

    private Instant publishedAt;

    private int attempts;

    public OutboxMessage(UUID id, String exchange, String routingKey, String headersJson, String payloadJson, Instant createdAt, Instant publishedAt, int attempts) {
        this.id = id;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.headersJson = headersJson;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
        this.attempts = attempts;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getHeadersJson() {
        return headersJson;
    }

    public void setHeadersJson(String headersJson) {
        this.headersJson = headersJson;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
}