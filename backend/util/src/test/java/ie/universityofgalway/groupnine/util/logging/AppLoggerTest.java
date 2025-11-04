package ie.universityofgalway.groupnine.util.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AppLoggerTest {

    @Test
    void withCorrelationId_sets_and_restores_mdc() throws Exception {
        MDC.clear();
        // Some test runtimes bind a NOP MDC adapter; if so, skip assertions
        MDC.put("cid", "probe");
        boolean mdcWorks = "probe".equals(MDC.get("cid"));
        MDC.clear();

        try (AutoCloseable c = AppLogger.withCorrelationId("abc")) {
            if (mdcWorks) {
                assertEquals("abc", MDC.get("cid"));
            }
        }
        if (mdcWorks) assertNull(MDC.get("cid"));

        // when there was a previous value, it is restored
        if (mdcWorks) {
            MDC.put("cid", "prev");
            try (AutoCloseable c = AppLogger.withCorrelationId("now")) {
                assertEquals("now", MDC.get("cid"));
            }
            assertEquals("prev", MDC.get("cid"));
            MDC.clear();
        }
    }

    @Test
    void format_renders_key_value_pairs_and_sanitizes_keys() throws Exception {
        Method m = AppLogger.class.getDeclaredMethod("format", String.class, Object[].class);
        m.setAccessible(true);
        String s = (String) m.invoke(null, "event", new Object[]{"user id", 123, "ok", true});
        assertTrue(s.startsWith("event "));
        assertTrue(s.contains("user_id=123"));
        assertTrue(s.contains("ok=true"));
    }
}
