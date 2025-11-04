package ie.universityofgalway.groupnine.delivery.webhook;

import ie.universityofgalway.groupnine.service.payments.webhook.port.StripeEventParserPort;
import ie.universityofgalway.groupnine.service.payments.webhook.usecase.ProcessStripeWebhookUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StripeWebhookControllerTest {

    private MockMvc mockMvc;
    private ProcessStripeWebhookUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = Mockito.mock(ProcessStripeWebhookUseCase.class);
        StripeWebhookController controller = new StripeWebhookController(useCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void receive_ok_onSuccessfulProcessing() throws Exception {
        String payload = "{\"type\":\"checkout.session.completed\"}";
        String sig = "t=123,v1=deadbeef";

        mockMvc.perform(post("/api/v1/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", sig)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        ArgumentCaptor<String> payloadCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sigCap = ArgumentCaptor.forClass(String.class);
        verify(useCase).execute(payloadCap.capture(), sigCap.capture());
        assertEquals(payload, payloadCap.getValue());
        assertEquals(sig, sigCap.getValue());
    }

    @Test
    void receive_400_onInvalidSignature() throws Exception {
        doThrow(new StripeEventParserPort.InvalidSignatureException("bad sig"))
                .when(useCase).execute(anyString(), anyString());

        mockMvc.perform(post("/api/v1/webhooks/stripe")
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Stripe-Signature", "v1=bad")
                        .content("rawbody"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("invalid signature"));
    }

    @Test
    void receive_200_ignored_onGenericError() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(useCase).execute(anyString(), anyString());

        mockMvc.perform(post("/api/v1/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "t=1,v1=x")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("ignored"));
    }
}

