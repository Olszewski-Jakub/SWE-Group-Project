package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.delivery.rest.auth.dto.ChangePasswordRequest;
import ie.universityofgalway.groupnine.delivery.rest.support.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.service.auth.usecase.ChangePasswordUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChangePasswordControllerTest {
    MockMvc mockMvc;
    ChangePasswordUseCase useCase;
    AccessTokenUserResolver resolver;

    @BeforeEach
    void setup() {
        useCase = Mockito.mock(ChangePasswordUseCase.class);
        resolver = Mockito.mock(AccessTokenUserResolver.class);
        ChangePasswordController controller = new ChangePasswordController(useCase, resolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void changes_password_when_authenticated() throws Exception {
        UserId id = UserId.newId();
        User user = new User(id, Email.of("jane@example.com"), "Jane", "Doe", UserStatus.ACTIVE, true, "hash", Instant.now(), Instant.now());
        when(resolver.requireUser(any())).thenReturn(user);

        String json = "{\n  \"currentPassword\": \"curr\",\n  \"newPassword\": \"new-password-123\"\n}";
        mockMvc.perform(post("/api/v1/auth/change-password").contentType(MediaType.APPLICATION_JSON).content(json)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));
        Mockito.verify(useCase).execute(eq(id), eq("curr"), eq("new-password-123"));
    }
}
