package ie.universityofgalway.groupnine.delivery.rest.rbac;

import ie.universityofgalway.groupnine.delivery.rest.customer.CustomerController;
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
 * RBAC tests for customer routes: verifies authentication requirement and
 * that only the CUSTOMER role can access the customer ping endpoint.
 */
@DeliveryWebMvcTest(controllers = CustomerController.class)
@Import(RbacTestSecurityConfig.class)
class RbacCustomerAccessTest extends CommonWebMvcTest {

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
    void requires_authentication_for_customer_route() throws Exception {
        mockMvc.perform(get("/api/customer/ping").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void denies_staff_for_customer_route() throws Exception {
        try (var ignored = withRoles("STAFF")) {
            mockMvc.perform(get("/api/customer/ping").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void allows_customer_role_for_customer_route() throws Exception {
        try (var ignored = withRoles("CUSTOMER")) {
            mockMvc.perform(get("/api/customer/ping").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}
