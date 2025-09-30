package ie.universityofgalway.groupnine.infrastructure.security.adapter;

import ie.universityofgalway.groupnine.security.config.props.AppSecurityProps;
import ie.universityofgalway.groupnine.security.jwt.JwtService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JwtAccessTokenAdapterTest {
    @Test
    void delegatesToJwtServiceAndExposesTtl() {
        JwtService jwt = mock(JwtService.class);
        AppSecurityProps props = new AppSecurityProps();
        AppSecurityProps.Jwt jwtProps = new AppSecurityProps.Jwt();
        jwtProps.setAccessTokenTtl(Duration.ofSeconds(900));
        props.setJwt(jwtProps);
        when(jwt.createAccessToken(anyString(), anyList(), any())).thenReturn("tok");

        JwtAccessTokenAdapter adapter = new JwtAccessTokenAdapter(jwt, props);
        String tok = adapter.createAccessToken("u", java.util.List.of(), java.util.Map.of());
        assertEquals("tok", tok);
        assertEquals(900L, adapter.getAccessTokenTtlSeconds());
    }
}

