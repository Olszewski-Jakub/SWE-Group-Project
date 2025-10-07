package ie.universityofgalway.groupnine.domain.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for public HTTP endpoints.
 * <p>
 * When present on a controller class or method, the {@code AuthGuardInterceptor}
 * allows anonymous access and skips the authentication requirement.
 * Use sparingly and prefer specific methods over broad class-level annotations.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicEndpoint {
}
