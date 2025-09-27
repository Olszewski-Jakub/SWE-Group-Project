package ie.universityofgalway.groupnine.util.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Uniform logging facade with simple key=value support and correlation id handling.
 */
public final class AppLogger {
    private static final String CID = "cid";
    private final Logger log;

    private AppLogger(Logger log) {
        this.log = log;
    }

    public static AppLogger get(Class<?> type) {
        return new AppLogger(LoggerFactory.getLogger(type));
    }

    public static AutoCloseable withCorrelationId(String correlationId) {
        final String previous = MDC.get(CID);
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CID, correlationId);
        }
        return () -> {
            if (previous == null) MDC.remove(CID);
            else MDC.put(CID, previous);
        };
    }

    private static String format(String message, Object... kvPairs) {
        StringJoiner joiner = new StringJoiner(" ", message + " ", "");
        List<Object> list = new ArrayList<>();
        if (kvPairs != null) {
            for (int i = 0; i < kvPairs.length; i += 2) {
                Object k = kvPairs[i];
                Object v = (i + 1 < kvPairs.length) ? kvPairs[i + 1] : null;
                if (k != null) {
                    joiner.add(safeKey(k) + "=" + String.valueOf(v));
                }
            }
        }
        return joiner.toString().trim();
    }

    private static String safeKey(Object k) {
        String s = Objects.toString(k);
        return s.replaceAll("\\s+", "_");
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public void debug(String message, Object... kvPairs) {
        log.debug(format(message, kvPairs));
    }

    public void info(String message, Object... kvPairs) {
        log.info(format(message, kvPairs));
    }

    public void warn(String message, Object... kvPairs) {
        log.warn(format(message, kvPairs));
    }

    public void error(String message, Object... kvPairs) {
        log.error(format(message, kvPairs));
    }
}

