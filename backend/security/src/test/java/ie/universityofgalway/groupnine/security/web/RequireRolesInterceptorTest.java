package ie.universityofgalway.groupnine.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.domain.security.RequireRoles;
import ie.universityofgalway.groupnine.domain.user.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RequireRolesInterceptorTest {

    private final JsonAuthHandlers handlers = new JsonAuthHandlers(new ObjectMapper());
    private final RequireRolesInterceptor interceptor = new RequireRolesInterceptor(handlers);

    @AfterEach
    void cleanupContext() {
        SecurityContextHolder.clearContext();
    }

    static class Controller {
        @RequireRoles({Role.MANAGER})
        public void manage() {}

        public void open() {}
    }

    @RequireRoles({Role.SUPPORT})
    static class SupportController {
        public void ticket() {}
    }

    @Test
    void noRequirementAllowsAccess() throws Exception {
        HandlerMethod hm = handler(new Controller(), "open");
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(req, res, hm);
        assertThat(allowed).isTrue();
    }

    @Test
    void blocksWhenNoAuthPresent() throws Exception {
        HandlerMethod hm = handler(new Controller(), "manage");
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
        boolean allowed = interceptor.preHandle(req, res, hm);
        assertThat(allowed).isFalse();
        assertThat(res.getStatus()).isEqualTo(401);
    }

    @Test
    void forbidsWhenInsufficientRole() throws Exception {
        HandlerMethod hm = handler(new Controller(), "manage");
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        var customer = new UsernamePasswordAuthenticationToken("u", "t",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(customer);

        boolean allowed = interceptor.preHandle(req, res, hm);
        assertThat(allowed).isFalse();
        assertThat(res.getStatus()).isEqualTo(403);
    }

    @Test
    void allowsWhenRoleMatches() throws Exception {
        HandlerMethod hm = handler(new Controller(), "manage");
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        var manager = new UsernamePasswordAuthenticationToken("u", "t",
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));
        SecurityContextHolder.getContext().setAuthentication(manager);

        boolean allowed = interceptor.preHandle(req, res, hm);
        assertThat(allowed).isTrue();
    }

    @Test
    void readsTypeLevelAnnotation() throws Exception {
        HandlerMethod hm = handler(new SupportController(), "ticket");
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        var support = new UsernamePasswordAuthenticationToken("u", "t",
                List.of(new SimpleGrantedAuthority("ROLE_SUPPORT")));
        SecurityContextHolder.getContext().setAuthentication(support);
        boolean allowed = interceptor.preHandle(req, res, hm);
        assertThat(allowed).isTrue();
    }

    private HandlerMethod handler(Object controller, String name) throws NoSuchMethodException {
        Method m = controller.getClass().getMethod(name);
        return new HandlerMethod(controller, m);
    }
}

