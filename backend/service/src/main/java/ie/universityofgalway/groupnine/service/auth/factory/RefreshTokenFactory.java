package ie.universityofgalway.groupnine.service.auth.factory;

import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;

public class RefreshTokenFactory {
    private final RandomTokenPort randomTokenPort;

    public RefreshTokenFactory(RandomTokenPort randomTokenPort) {
        this.randomTokenPort = randomTokenPort;
    }

    /**
     * Generates a new 32-byte opaque refresh token and its SHA-256 hash.
     */
    public Pair generate() {
        String token = randomTokenPort.generateOpaqueToken();
        String hash = randomTokenPort.sha256(token);
        return new Pair(token, hash);
    }

    public record Pair(String token, String hash) {
    }
}

