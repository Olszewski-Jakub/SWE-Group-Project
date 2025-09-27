package ie.universityofgalway.groupnine.buildlogic.convention.library

import ie.universityofgalway.groupnine.buildlogic.convention.configuration.configureIntegration
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.applyIfMissing
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.configureJava17ToolchainIfPresent
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class IntegrationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        configureJava17ToolchainIfPresent()

        apply(plugin = libs.plugin("convention-testing"))
        apply(plugin = libs.plugin("convention-jacoco"))

        applyIfMissing(
            libs.plugin("spring-boot"),
            libs.plugin("spring-boot-dependencies")
        )

        configureIntegration()
    }
}
