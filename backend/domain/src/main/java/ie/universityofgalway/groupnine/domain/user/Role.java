package ie.universityofgalway.groupnine.domain.user;

/**
 * Domain role enumeration. Independent of any framework.
 */
public enum Role {
    CUSTOMER,
    STAFF,
    MANAGER,
    ADMIN,
    SUPPORT;

    /**
     * Expands a collection of roles using the domain role hierarchy rules.
     * <p>
     * Hierarchy (from strongest to narrowest):
     * - ADMIN implies MANAGER, STAFF, SUPPORT (but not CUSTOMER)
     * - MANAGER implies STAFF, SUPPORT
     * - STAFF implies STAFF
     * - SUPPORT implies SUPPORT
     * - CUSTOMER implies CUSTOMER
     * The returned set always contains the original roles.
     */
    public static java.util.Set<Role> expandHierarchy(java.util.Collection<Role> roles) {
        java.util.Set<Role> result = new java.util.HashSet<>();
        if (roles == null) return result;
        for (Role r : roles) {
            if (r == null) continue;
            switch (r) {
                case ADMIN -> {
                    result.add(ADMIN);
                    result.add(MANAGER);
                    result.add(STAFF);
                    result.add(SUPPORT);
                }
                case MANAGER -> {
                    result.add(MANAGER);
                    result.add(STAFF);
                    result.add(SUPPORT);
                }
                case STAFF -> result.add(STAFF);
                case SUPPORT -> result.add(SUPPORT);
                case CUSTOMER -> result.add(CUSTOMER);
                default -> result.add(r);
            }
        }
        return result;
    }
}
