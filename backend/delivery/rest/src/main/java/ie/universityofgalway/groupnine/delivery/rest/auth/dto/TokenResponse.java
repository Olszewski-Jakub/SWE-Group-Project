package ie.universityofgalway.groupnine.delivery.rest.auth.dto;

public record TokenResponse(String accessToken, long expiresIn, String refreshToken, String tokenType) {}

