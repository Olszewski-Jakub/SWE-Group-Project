package ie.universityofgalway.groupnine.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.security.config.props.AppSecurityProps;
import ie.universityofgalway.groupnine.security.jwt.JwtClaims;
import ie.universityofgalway.groupnine.security.jwt.JwtException;
import ie.universityofgalway.groupnine.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final AppSecurityProps props = new AppSecurityProps();
    private final JsonAuthHandlers handlers = new JsonAuthHandlers(new ObjectMapper());
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtService, props, handlers);

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void setsAuthenticationOnValidBearer() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer abc");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        Map<String, Object> claims = Map.of(
                props.getJwt().getAuthoritiesClaim(), List.of("MANAGER")
        );
        when(jwtService.validate(anyString())).thenReturn(
                new JwtClaims("user-1", "iss", Instant.now(), Instant.now().plusSeconds(3600), List.of("MANAGER"), claims)
        );

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user-1");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(Object::toString)
                .anyMatch(s -> s.toString().contains("ROLE_MANAGER"));
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void returns401OnJwtException() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        when(jwtService.validate(anyString())).thenThrow(new JwtException("invalid"));

        filter.doFilterInternal(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentType()).isEqualTo("application/json");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void noHeaderProceedsWithoutAuthentication() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(res.getStatus()).isEqualTo(200);
    }
}
