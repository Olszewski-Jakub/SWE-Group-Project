package ie.universityofgalway.groupnine.delivery.rest.util;

/**
 * Centralized API route constants to ensure a single source of truth
 * for versioned paths (e.g., /api/v1/...).
 */
public final class Routes {
    private Routes() {}

    public static final String API = "/api";
    public static final String DEV = "/dev";
    public static final String V1 = API + "/v1";

    public static final String AUTH = V1 + "/auth";
    public static final String CART = V1 + "/cart";
    public static final String MANAGEMENT = V1 + "/management";
    public  static final String PRODUCT_MANAGEMENT = MANAGEMENT + "/products";

    // Role-protected base paths exposed under public API (no versioning)
    public static final String ADMIN = API + "/admin";
    public static final String STAFF = API + "/staff";
    public static final String MANAGER = API + "/manager";
    // Support endpoints remain under /dev for testing
    public static final String SUPPORT = DEV + "/support";
    public static final String CUSTOMER = API + "/customer";
}
