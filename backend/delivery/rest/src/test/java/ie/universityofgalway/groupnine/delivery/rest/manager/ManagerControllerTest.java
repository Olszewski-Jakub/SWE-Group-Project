package ie.universityofgalway.groupnine.delivery.rest.manager;

import ie.universityofgalway.groupnine.delivery.rest.dev.manager.ManagerController;
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

@DeliveryWebMvcTest(controllers = ManagerController.class)
@Import(RbacTestSecurityConfig.class)
class ManagerControllerTest extends CommonWebMvcTest {

    @Test
    void requiresManagerRole() throws Exception {
        // No auth -> 401
        mockMvc.perform(get("/api/manager/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // CUSTOMER -> 403
        var auth = new UsernamePasswordAuthenticationToken("u","t", java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc.perform(get("/api/manager/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // MANAGER -> 200
        auth = new UsernamePasswordAuthenticationToken("u","t", java.util.List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc.perform(get("/api/manager/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

