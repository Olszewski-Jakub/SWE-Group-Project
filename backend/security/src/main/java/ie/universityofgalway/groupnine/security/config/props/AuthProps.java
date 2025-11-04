package ie.universityofgalway.groupnine.security.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.auth")
@Validated
public class AuthProps {
    private int refreshTtlDays = 14;
    private String refreshCookieName = "refreshToken";
    private boolean cookieSecure = true;
    private String cookieSameSite = "None";

    public int getRefreshTtlDays() {
        return refreshTtlDays;
    }

    public void setRefreshTtlDays(int refreshTtlDays) {
        this.refreshTtlDays = refreshTtlDays;
    }

    public String getRefreshCookieName() {
        return refreshCookieName;
    }

    public void setRefreshCookieName(String refreshCookieName) {
        this.refreshCookieName = refreshCookieName;
    }

    public boolean isCookieSecure() {
        return cookieSecure;
    }

    public void setCookieSecure(boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
    }

    public String getCookieSameSite() {
        return cookieSameSite;
    }

    public void setCookieSameSite(String cookieSameSite) {
        this.cookieSameSite = cookieSameSite;
    }
}
