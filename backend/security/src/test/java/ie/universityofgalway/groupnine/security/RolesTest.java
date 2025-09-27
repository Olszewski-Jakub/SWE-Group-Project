package ie.universityofgalway.groupnine.security;

import ie.universityofgalway.groupnine.security.core.Roles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RolesTest {

    @Test
    void adminRoleConstantIsCorrect() {
        Assertions.assertEquals("ADMIN", Roles.ADMIN);
    }

    @Test
    void userRoleConstantIsCorrect() {
        Assertions.assertEquals("USER", Roles.USER);
    }
}
