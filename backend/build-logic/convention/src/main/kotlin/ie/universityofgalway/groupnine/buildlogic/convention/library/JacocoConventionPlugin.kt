package ie.universityofgalway.groupnine.buildlogic.convention.library

import ie.universityofgalway.groupnine.buildlogic.convention.configuration.configureJacoco
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.applyIfMissing
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project

open class CoverageExtension {
    /**
     * Single minimum coverage threshold (0.0..1.0) applied to all counters.
     * Example: 0.80 = 80%
     */
    var minimum: Double = 0.0

    var counters: List<String> = listOf("LINE", "BRANCH", "INSTRUCTION", "METHOD", "CLASS", "COMPLEXITY")
}

class JacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        applyIfMissing(libs.plugin("jacoco"))

        configureJacoco()
    }
}
