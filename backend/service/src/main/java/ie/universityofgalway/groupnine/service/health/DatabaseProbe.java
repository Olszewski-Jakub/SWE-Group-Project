package ie.universityofgalway.groupnine.service.health;

public interface DatabaseProbe {
    /** @return true if DB responds to a trivial query. */
    boolean pingDatabase();
}