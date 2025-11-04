package ie.universityofgalway.groupnine.service.auth.port;

public interface OAuthStatePort {
    String create(String redirect);
    Parsed verify(String stateToken);

    record Parsed(String nonce, String redirect) {}
}

