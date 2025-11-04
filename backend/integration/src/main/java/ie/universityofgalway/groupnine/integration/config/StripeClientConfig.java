package ie.universityofgalway.groupnine.integration.config;

import com.stripe.Stripe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Centralizes Stripe API initialization so adapters don't manage global state.
 */
@Configuration
public class StripeClientConfig {
    private static final Logger LOG = LoggerFactory.getLogger(StripeClientConfig.class);

    @Value("${stripe.secret:}")
    private String secret;

    @PostConstruct
    public void initStripe() {
        if (secret != null && !secret.isBlank()) {
            Stripe.apiKey = secret;
            LOG.info("Stripe API key configured");
        } else {
            LOG.warn("stripe_api_key_missing: set stripe.secret or STRIPE_SECRET env to enable Checkout");
        }
    }
}

