package ie.universityofgalway.groupnine.service.messaging.port;

/**
 * Port for recording and checking processed inbound events for idempotency.
 */
public interface ProcessedEventPort {
    boolean alreadyProcessed(String source, String key);
    void markProcessed(String source, String key);
}

