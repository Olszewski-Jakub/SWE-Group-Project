package ie.universityofgalway.groupnine.delivery.rest.support;

/**
 * Centralized API route constants to ensure a single source of truth
 * for versioned paths (e.g., /api/v1/...).
 */
public final class Routes {
    private Routes() {}

    public static final String API = "/api";
    public static final String V1 = API + "/v1";

    public static final String AUTH = V1 + "/auth";
    public static final String ADMIN = API + "/admin";
    public static final String STAFF = API + "/staff";
    public static final String MANAGER = API + "/manager";
    public static final String SUPPORT = API + "/support";
    public static final String CUSTOMER = API + "/customer";
}
