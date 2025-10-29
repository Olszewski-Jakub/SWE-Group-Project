package ie.universityofgalway.groupnine.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.security.PublicEndpoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthGuardInterceptorTest {

    private final JsonAuthHandlers handlers = new JsonAuthHandlers(new ObjectMapper());
    private final AuthGuardInterceptor interceptor = new AuthGuardInterceptor(handlers);

    @AfterEach
    void cleanupContext() {
        SecurityContextHolder.clearContext();
    }

    @PublicEndpoint
    static class PublicController {
        public void ping() {}
    }

    static class MixedController {
        @PublicEndpoint
        public void open() {}
        public void secure() {}
    }

    @Test
    void allowsPublicControllerWithoutAuth() throws Exception {
        HandlerMethod hm = handler(new PublicController(), "ping");
        HttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(req, res, hm);

        assertThat(allowed).isTrue();
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void allowsPublicMethodWithoutAuth() throws Exception {
        HandlerMethod hm = handler(new MixedController(), "open");
        HttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(req, res, hm);

        assertThat(allowed).isTrue();
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void blocksUnauthenticatedAccessToSecureMethod() throws Exception {
        HandlerMethod hm = handler(new MixedController(), "secure");
        HttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();

        boolean allowed = interceptor.preHandle(req, res, hm);

        assertThat(allowed).isFalse();
        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentType()).isEqualTo("application/json");
    }

    @Test
    void blocksAnonymousAuthentication() throws Exception {
        HandlerMethod hm = handler(new MixedController(), "secure");
        HttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        var anon = new AnonymousAuthenticationToken("key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anon);

        boolean allowed = interceptor.preHandle(req, res, hm);

        assertThat(allowed).isFalse();
        assertThat(res.getStatus()).isEqualTo(401);
    }

    @Test
    void allowsAuthenticatedNonAnonymous() throws Exception {
        HandlerMethod hm = handler(new MixedController(), "secure");
        HttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        var auth = new UsernamePasswordAuthenticationToken("user", "token", List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        boolean allowed = interceptor.preHandle(req, res, hm);

        assertThat(allowed).isTrue();
        assertThat(res.getStatus()).isEqualTo(200);
    }

    private HandlerMethod handler(Object controller, String name) throws NoSuchMethodException {
        Method m = controller.getClass().getMethod(name);
        return new HandlerMethod(controller, m);
    }
}

