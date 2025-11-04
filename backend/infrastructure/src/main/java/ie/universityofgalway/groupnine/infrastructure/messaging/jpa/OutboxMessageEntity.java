package ie.universityofgalway.groupnine.infrastructure.messaging.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnTransformer;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
public class OutboxMessageEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "exchange", nullable = false)
    private String exchange;

    @Column(name = "routing_key", nullable = false)
    private String routingKey;

    @Column(name = "headers_json", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String headersJson;

    @Column(name = "payload_json", columnDefinition = "jsonb", nullable = false)
    @ColumnTransformer(write = "?::jsonb")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    private OutboxMessageEntity(UUID id, String exchange, String routingKey, String headersJson, String payloadJson, Instant createdAt, Instant publishedAt, int attempts) {
        this.id = id;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.headersJson = headersJson;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
        this.attempts = attempts;
    }

    public OutboxMessageEntity() {

    }

    public static OutboxMessageEntity of(UUID id, String exchange, String routingKey, String headersJson, String payloadJson, Instant createdAt, Instant publishedAt, int attempts) {
        return new OutboxMessageEntity(id, exchange, routingKey, headersJson, payloadJson, createdAt, publishedAt, attempts);
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

