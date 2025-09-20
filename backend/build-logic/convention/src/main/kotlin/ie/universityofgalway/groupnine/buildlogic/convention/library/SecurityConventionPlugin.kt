package ie.universityofgalway.groupnine.buildlogic.convention.library

import ie.universityofgalway.groupnine.buildlogic.convention.configuration.configureSecurity
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.applyIfMissing
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.compileOnly
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.configureJava17ToolchainIfPresent
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.library
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.plugin
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies


class SecurityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        configureJava17ToolchainIfPresent()

        applyIfMissing(
            libs.plugin("java-library"),
            libs.plugin("spring-boot-dependencies")
        )

        apply(plugin = libs.plugin("convention-testing"))
        apply(plugin = libs.plugin("convention-jacoco"))

        configureSecurity()
    }
}