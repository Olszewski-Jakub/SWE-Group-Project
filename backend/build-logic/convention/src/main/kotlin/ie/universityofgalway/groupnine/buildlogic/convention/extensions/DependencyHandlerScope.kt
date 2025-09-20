package ie.universityofgalway.groupnine.buildlogic.convention.extensions

import org.gradle.kotlin.dsl.DependencyHandlerScope

/** Mirror the common Gradle configurations so they're usable in build-logic. */
fun DependencyHandlerScope.implementation(dep: Any) = add("implementation", dep)
fun DependencyHandlerScope.api(dep: Any)            = add("api", dep)
fun DependencyHandlerScope.compileOnly(dep: Any)    = add("compileOnly", dep)
fun DependencyHandlerScope.runtimeOnly(dep: Any)    = add("runtimeOnly", dep)

fun DependencyHandlerScope.testImplementation(dep: Any) = add("testImplementation", dep)
fun DependencyHandlerScope.testRuntimeOnly(dep: Any)    = add("testRuntimeOnly", dep)

/** Add the same dependency to multiple configurations at once. */
fun DependencyHandlerScope.addToAll(configs: Iterable<String>, dep: Any) {
    configs.forEach { add(it, dep) }
}
