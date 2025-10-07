package ie.universityofgalway.groupnine.domain.security;

import ie.universityofgalway.groupnine.domain.user.Role;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares required domain roles on controller types or methods.
 * <p>
 * Enforcement is performed by the security module at the MVC layer so that REST code
 * remains free from Spring Security annotations. Example usage:
 * <pre>
 * {@code @RequireRoles({Role.ADMIN, Role.MANAGER})}
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRoles {
    Role[] value();
}
