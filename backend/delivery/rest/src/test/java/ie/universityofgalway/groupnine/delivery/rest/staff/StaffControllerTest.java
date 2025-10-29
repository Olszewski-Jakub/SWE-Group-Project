package ie.universityofgalway.groupnine.delivery.rest.staff;

import ie.universityofgalway.groupnine.delivery.rest.dev.staff.StaffController;
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

@DeliveryWebMvcTest(controllers = StaffController.class)
@Import(RbacTestSecurityConfig.class)
class StaffControllerTest extends CommonWebMvcTest {
    @Test
    void requiresStaffRole() throws Exception {
        mockMvc.perform(get("/api/staff/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        var auth = new UsernamePasswordAuthenticationToken("u","t", java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc.perform(get("/api/staff/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        auth = new UsernamePasswordAuthenticationToken("u","t", java.util.List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc.perform(get("/api/staff/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

