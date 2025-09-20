package ie.universityofgalway.groupnine.security;

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
