package ie.universityofgalway.groupnine.testsupport.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for MVC slice tests.
 * - Autowires MockMvc and ObjectMapper
 * - Provides JSON helpers
 * - Dirties the context after the class to force a clean restart next class
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class CommonWebMvcTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    protected String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
