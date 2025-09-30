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
}

