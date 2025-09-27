package ie.universityofgalway.groupnine.infrastructure.auth.factory;

import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenFactoryImplTest {

    @Test
    void delegates_to_service_factory_and_returns_opaque_plus_hash() {
        RandomTokenPort random = new RandomTokenPort() {
            @Override
            public String generateOpaqueToken() {
                return "opaque";
            }

            @Override
            public String sha256(String token) {
                return "hash-" + token;
            }
        };
        TokenFactoryImpl bean = new TokenFactoryImpl(random);
        var res = bean.createEmailVerification(UserId.newId(), Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-02T00:00:00Z"));
        assertEquals("opaque", res.opaque());
        assertTrue(res.token().tokenHash().startsWith("hash-"));
    }
}

