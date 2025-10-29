package ie.universityofgalway.groupnine.delivery.rest.admin;

import ie.universityofgalway.groupnine.delivery.rest.dev.admin.AdminRoleController;
import ie.universityofgalway.groupnine.delivery.rest.rbac.RbacTestSecurityConfig;
import ie.universityofgalway.groupnine.service.auth.usecase.AssignRoleToUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.GetUserRolesUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.RevokeRoleFromUserUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DeliveryWebMvcTest(controllers = AdminRoleController.class)
@Import(RbacTestSecurityConfig.class)
class AdminRoleControllerTest extends CommonWebMvcTest {

    @MockitoBean
    AssignRoleToUserUseCase assign;
    @MockitoBean RevokeRoleFromUserUseCase revoke;
    @MockitoBean GetUserRolesUseCase getRoles;

    private void asAdmin() {
        var auth = new UsernamePasswordAuthenticationToken("u","t", java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void list_assign_revoke_flow() throws Exception {
        UUID uid = UUID.randomUUID();
        asAdmin();
        when(getRoles.execute(any())).thenReturn(new GetUserRolesUseCase.Result(uid.toString(), List.of("CUSTOMER")));
        mockMvc.perform(get("/api/admin/users/{userId}/roles", uid).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(uid.toString()))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));

        when(assign.execute(any(), any())).thenReturn(new AssignRoleToUserUseCase.Result(uid.toString(), List.of("ADMIN","CUSTOMER")));
        mockMvc.perform(post("/api/admin/users/{userId}/roles/{role}", uid, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray());

        when(revoke.execute(any(), any())).thenReturn(new AssignRoleToUserUseCase.Result(uid.toString(), List.of("CUSTOMER")));
        mockMvc.perform(delete("/api/admin/users/{userId}/roles/{role}", uid, "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
    }
}

