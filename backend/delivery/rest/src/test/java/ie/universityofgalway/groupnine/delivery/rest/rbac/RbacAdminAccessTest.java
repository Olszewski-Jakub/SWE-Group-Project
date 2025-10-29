package ie.universityofgalway.groupnine.delivery.rest.rbac;

import ie.universityofgalway.groupnine.delivery.rest.dev.admin.AdminPingController;
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

/**
 * RBAC tests for admin routes: verifies 401 for anonymous, 403 for non-admin,
 * and 200 for admin role on a simple ping endpoint.
 */
@DeliveryWebMvcTest(controllers = AdminPingController.class)
@Import(RbacTestSecurityConfig.class)
class RbacAdminAccessTest extends CommonWebMvcTest {

    private AutoCloseable withRoles(String... roles) {
        var auth = new UsernamePasswordAuthenticationToken(
                "160c0902-24b7-469e-89ea-06f7a5cc98f5",
                "token",
                java.util.Arrays.stream(roles)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        return () -> SecurityContextHolder.clearContext();
    }

    @Test
    void requires_authentication_for_admin_route() throws Exception {
        // No auth set → 401
        mockMvc.perform(get("/api/admin/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void denies_customer_role_for_admin_route() throws Exception {
        try (var ignored = withRoles("CUSTOMER")) {
            mockMvc.perform(get("/api/admin/ping").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void allows_admin_role_for_admin_route() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            mockMvc.perform(get("/api/admin/ping").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}
