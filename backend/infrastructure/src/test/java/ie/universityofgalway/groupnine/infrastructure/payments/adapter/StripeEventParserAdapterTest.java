package ie.universityofgalway.groupnine.infrastructure.payments.adapter;

import ie.universityofgalway.groupnine.service.payments.webhook.port.StripeEventParserPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StripeEventParserAdapterTest {

    @Test
    void parse_throws_when_secret_missing() {
        StripeEventParserAdapter adapter = new StripeEventParserAdapter("");
        assertThrows(StripeEventParserPort.InvalidSignatureException.class,
                () -> adapter.parse("{}", "sig"));
    }
}

