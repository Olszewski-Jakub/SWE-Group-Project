package ie.universityofgalway.groupnine.infrastructure.messaging.jpa;

import java.io.Serializable;
import java.util.Objects;

public class ProcessedEventId implements Serializable {
    private String source;
    private String key;

    public ProcessedEventId() {
    }

    public ProcessedEventId(String source, String key) {
        this.source = source;
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessedEventId that = (ProcessedEventId) o;
        return Objects.equals(source, that.source) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, key);
    }
}

