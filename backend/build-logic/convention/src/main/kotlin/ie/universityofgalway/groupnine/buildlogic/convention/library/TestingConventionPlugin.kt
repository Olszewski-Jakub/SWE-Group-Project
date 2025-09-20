package ie.universityofgalway.groupnine.buildlogic.convention.library

import ie.universityofgalway.groupnine.buildlogic.convention.configuration.configureTesting
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.applyIfMissing
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.configureJava17ToolchainIfPresent
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        applyIfMissing(
            libs.plugin("java"),
        )

        configureJava17ToolchainIfPresent()

        configureTesting()
    }
}
