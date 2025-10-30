package ie.universityofgalway.groupnine.delivery.rest.checkout;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.delivery.rest.checkout.dto.CheckoutSessionRequest;
import ie.universityofgalway.groupnine.delivery.rest.util.GlobalExceptionHandler;
import ie.universityofgalway.groupnine.delivery.rest.util.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.order.OrderId;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.service.payments.dto.StartCheckoutResultDto;
import ie.universityofgalway.groupnine.service.payments.usecase.StartCheckoutUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CheckoutControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private AccessTokenUserResolver userResolver;
    private StartCheckoutUseCase startCheckout;

    private User testUser;

    @BeforeEach
    void setup() {
        userResolver = Mockito.mock(AccessTokenUserResolver.class);
        startCheckout = Mockito.mock(StartCheckoutUseCase.class);

        UserId uid = UserId.of(UUID.randomUUID());
        testUser = new User(uid, Email.of("buyer@example.com"), "Buyer", "User", UserStatus.ACTIVE, true, "hash", Instant.now(), Instant.now());
        when(userResolver.requireUser(any())).thenReturn(testUser);

        String baseUrl = "https://shop.example.com";
        String successPath = "/checkout/success";
        String cancelPath = "/checkout/cancel";

        CheckoutController controller = new CheckoutController(userResolver, startCheckout, baseUrl, successPath, cancelPath);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createSession_returns201_andLocation_andBody() throws Exception {
        UUID cartUuid = UUID.randomUUID();
        CheckoutSessionRequest req = new CheckoutSessionRequest(cartUuid);

        OrderId orderId = OrderId.of(UUID.randomUUID());
        String sessionId = "cs_test_abc";
        String checkoutUrl = "https://checkout.example.com/s/cs_test_abc";

        when(startCheckout.execute(eq(CartId.of(cartUuid)), eq(testUser.getId()), eq("https://shop.example.com/checkout/success"), eq("https://shop.example.com/checkout/cancel")))
                .thenReturn(new StartCheckoutResultDto(orderId, sessionId, checkoutUrl));

        mockMvc.perform(post("/api/v1/checkout/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, checkoutUrl))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.checkoutUrl").value(checkoutUrl));
    }

    @Test
    void createSession_joinsUrls_properly() throws Exception {
        // Rebuild controller with trailing base slash and leading path slash to exercise join logic
        AccessTokenUserResolver ur = userResolver;
        StartCheckoutUseCase sc = startCheckout;
        CheckoutController controller = new CheckoutController(ur, sc, "https://shop.example.com/", "/thanks", "/abort");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        UUID cartUuid = UUID.randomUUID();
        CheckoutSessionRequest req = new CheckoutSessionRequest(cartUuid);

        OrderId orderId = OrderId.of(UUID.randomUUID());
        String sessionId = "cs_test_123";
        String checkoutUrl = "https://checkout.example.com/s/cs_test_123";

        when(startCheckout.execute(any(CartId.class), eq(testUser.getId()), any(String.class), any(String.class)))
                .thenReturn(new StartCheckoutResultDto(orderId, sessionId, checkoutUrl));

        mockMvc.perform(post("/api/v1/checkout/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> successCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> cancelCaptor = ArgumentCaptor.forClass(String.class);
        verify(startCheckout).execute(eq(CartId.of(cartUuid)), eq(testUser.getId()), successCaptor.capture(), cancelCaptor.capture());
        assertEquals("https://shop.example.com/thanks", successCaptor.getValue());
        assertEquals("https://shop.example.com/abort", cancelCaptor.getValue());
    }

    @Test
    void createSession_returns400_whenCartIdMissing() throws Exception {
        String body = "{}"; // cartId null -> validation should fail
        mockMvc.perform(post("/api/v1/checkout/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}

