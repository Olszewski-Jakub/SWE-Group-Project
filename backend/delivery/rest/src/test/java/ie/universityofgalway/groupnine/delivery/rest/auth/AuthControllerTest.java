package ie.universityofgalway.groupnine.delivery.rest.auth;

import ie.universityofgalway.groupnine.domain.auth.EmailAlreadyUsed;
import ie.universityofgalway.groupnine.domain.auth.ExpiredVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.InvalidVerificationToken;
import ie.universityofgalway.groupnine.domain.auth.TokenAlreadyUsed;
import ie.universityofgalway.groupnine.service.auth.usecase.RegisterUserUseCase;
import ie.universityofgalway.groupnine.service.auth.usecase.VerifyEmailUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = {AuthController.class})
class AuthControllerTest extends CommonWebMvcTest {

    @MockitoBean
    RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    VerifyEmailUseCase verifyEmailUseCase;

    @Test
    void register_created() throws Exception {
        String body = "{\n" +
                " \"email\": \"john@example.com\",\n" +
                " \"password\": \"supersecurepwd\",\n" +
                " \"firstName\": \"John\",\n" +
                " \"lastName\": \"Doe\"\n" +
                "}";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        verify(registerUserUseCase).execute(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void register_conflict_if_email_used() throws Exception {
        doThrow(new EmailAlreadyUsed("Email already in use")).when(registerUserUseCase)
                .execute(anyString(), anyString(), anyString(), anyString());

        String body = "{\n" +
                " \"email\": \"john@example.com\",\n" +
                " \"password\": \"supersecurepwd\",\n" +
                " \"firstName\": \"John\",\n" +
                " \"lastName\": \"Doe\"\n" +
                "}";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void verify_ok() throws Exception {
        String body = "{\n \"token\": \"abc\"\n}";
        mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        verify(verifyEmailUseCase).execute(anyString());
    }

    @Test
    void verify_bad_request_on_invalid() throws Exception {
        doThrow(new InvalidVerificationToken("Invalid")).when(verifyEmailUseCase).execute(anyString());
        String body = "{\n \"token\": \"abc\"\n}";
        mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verify_gone_on_expired() throws Exception {
        doThrow(new ExpiredVerificationToken("Expired")).when(verifyEmailUseCase).execute(anyString());
        String body = "{\n \"token\": \"abc\"\n}";
        mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isGone());
    }

    @Test
    void verify_conflict_on_used() throws Exception {
        doThrow(new TokenAlreadyUsed("Used")).when(verifyEmailUseCase).execute(anyString());
        String body = "{\n \"token\": \"abc\"\n}";
        mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }
}
