package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.util.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = AuthMeController.class)
@Import(ie.universityofgalway.groupnine.delivery.rest.rbac.RbacTestSecurityConfig.class)
class AuthMeControllerTest extends CommonWebMvcTest {

    @MockitoBean
    private AccessTokenUserResolver userResolver;

    @Test
    void me_returns_user_details_and_roles_when_authenticated() throws Exception {
        // Arrange authenticated security context with roles
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("sub", "tok", authorities));

        // Mock resolved domain user
        UUID id = UUID.randomUUID();
        User user = new User(
                UserId.of(id),
                Email.of("jane.doe@example.com"),
                "Jane",
                "Doe",
                UserStatus.ACTIVE,
                true,
                null,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-02T00:00:00Z"),
                Set.of(Role.ADMIN, Role.CUSTOMER)
        );
        when(userResolver.requireUser(any())).thenReturn(user);

        // Act + Assert
        mockMvc.perform(get("/api/v1/auth/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.email", is("jane.doe@example.com")))
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "CUSTOMER")));
    }

    @Test
    void me_requires_authentication_returns_401_when_unauthenticated() throws Exception {
        // Ensure no authentication in context (CommonWebMvcTest clears before each test)
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/auth/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}

