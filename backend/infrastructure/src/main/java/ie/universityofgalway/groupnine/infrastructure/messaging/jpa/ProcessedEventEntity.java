package ie.universityofgalway.groupnine.infrastructure.messaging.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "processed_events")
@IdClass(ProcessedEventId.class)
public class ProcessedEventEntity {
    @Id
    @Column(name = "source", length = 64)
    private String source;

    @Id
    @Column(name = "key", length = 255)
    private String key;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
