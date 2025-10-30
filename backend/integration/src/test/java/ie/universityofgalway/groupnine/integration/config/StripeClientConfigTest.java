package ie.universityofgalway.groupnine.integration.config;

import com.stripe.Stripe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class StripeClientConfigTest {

    private String original;

    @BeforeEach
    void snapshot() {
        original = Stripe.apiKey;
    }

    @AfterEach
    void restore() {
        Stripe.apiKey = original;
    }

    @Test
    void initStripe_setsApiKey_whenSecretPresent() throws Exception {
        StripeClientConfig cfg = new StripeClientConfig();
        setSecret(cfg, "sk_test_123");

        cfg.initStripe();

        assertEquals("sk_test_123", Stripe.apiKey);
    }

    @Test
    void initStripe_noop_whenSecretMissingOrBlank() throws Exception {
        Stripe.apiKey = "old";

        StripeClientConfig cfgBlank = new StripeClientConfig();
        setSecret(cfgBlank, " ");
        cfgBlank.initStripe();
        assertEquals("old", Stripe.apiKey);

        StripeClientConfig cfgNull = new StripeClientConfig();
        setSecret(cfgNull, null);
        cfgNull.initStripe();
        assertEquals("old", Stripe.apiKey);
    }

    private static void setSecret(StripeClientConfig cfg, String value) throws Exception {
        Field f = StripeClientConfig.class.getDeclaredField("secret");
        f.setAccessible(true);
        f.set(cfg, value);
    }
}

