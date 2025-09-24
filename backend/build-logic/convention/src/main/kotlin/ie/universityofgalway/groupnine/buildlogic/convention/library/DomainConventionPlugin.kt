package ie.universityofgalway.groupnine.buildlogic.convention.library

import ie.universityofgalway.groupnine.buildlogic.convention.extensions.applyIfMissing
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.configureJava17ToolchainIfPresent
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class DomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        configureJava17ToolchainIfPresent()

        applyIfMissing("java-library")

        apply(plugin = libs.plugin("convention-testing"))
        apply(plugin = libs.plugin("convention-jacoco"))

        // Domain may log via shared utility (no Spring dependency)
        dependencies { implementation(project(":util")) }
    }
}
