package ie.universityofgalway.groupnine.delivery.rest.checkout;

import ie.universityofgalway.groupnine.delivery.rest.checkout.dto.CheckoutSessionRequest;
import ie.universityofgalway.groupnine.delivery.rest.checkout.dto.CheckoutSessionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Checkout DTOs")
class CheckoutDtoTest {

    @Test
    void checkoutSessionRequest_holdsCartId_andValidatesNotNull() {
        UUID id = UUID.randomUUID();
        CheckoutSessionRequest req = new CheckoutSessionRequest(id);
        assertEquals(id, req.cartId);

        try (ValidatorFactory vf = Validation.buildDefaultValidatorFactory()) {
            Validator validator = vf.getValidator();

            // valid when cartId present
            Set<ConstraintViolation<CheckoutSessionRequest>> ok = validator.validate(req);
            assertTrue(ok.isEmpty());

            // invalid when cartId is null
            CheckoutSessionRequest invalid = new CheckoutSessionRequest();
            invalid.cartId = null;
            Set<ConstraintViolation<CheckoutSessionRequest>> violations = validator.validate(invalid);
            assertFalse(violations.isEmpty());
        }
    }

    @Test
    void checkoutSessionResponse_setsAllFields() {
        String orderId = UUID.randomUUID().toString();
        String sessionId = "cs_test_123";
        String url = "https://checkout.example.com/s/cs_test_123";

        CheckoutSessionResponse res = new CheckoutSessionResponse(orderId, sessionId, url);

        assertEquals(orderId, res.orderId);
        assertEquals(sessionId, res.sessionId);
        assertEquals(url, res.checkoutUrl);
    }
}

