package ie.universityofgalway.groupnine.delivery.rest.supportteam;

import ie.universityofgalway.groupnine.delivery.rest.dev.supportteam.SupportController;
import ie.universityofgalway.groupnine.delivery.rest.rbac.RbacTestSecurityConfig;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = SupportController.class)
@Import(RbacTestSecurityConfig.class)
class SupportControllerTest extends CommonWebMvcTest {
    @Test
    void requiresSupportRole() throws Exception {
        mockMvc.perform(get("/dev/support/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        var auth = new UsernamePasswordAuthenticationToken("u","t", java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc.perform(get("/dev/support/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        auth = new UsernamePasswordAuthenticationToken("u","t", java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPPORT")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc.perform(get("/dev/support/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

