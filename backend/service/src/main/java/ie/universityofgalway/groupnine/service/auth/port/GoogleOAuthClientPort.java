package ie.universityofgalway.groupnine.service.auth.port;

public interface GoogleOAuthClientPort {
    TokenResponse exchangeCode(String code);
    UserInfo getUserInfo(String accessToken);

    class TokenResponse {
        public String accessToken;
        public String idToken;
        public String refreshToken;
        public String tokenType;
        public Long expiresIn;
    }

    class UserInfo {
        public String sub;
        public String email;
        public Boolean emailVerified;
        public String givenName;
        public String familyName;
        public String name;
        public String picture;
    }
}

