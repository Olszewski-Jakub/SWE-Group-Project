package ie.universityofgalway.groupnine.service.health;

/**
 * Output port for probing the health of the database.
 */
public interface DatabaseProbe {
    /**
     * @return true if the database responds to a trivial query.
     */
    boolean pingDatabase();

    /**
     * Optional extended DB details (vendor, version, pool metrics, etc.).
     */
    default java.util.Map<String, Object> details() { return java.util.Map.of(); }
}
