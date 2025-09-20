package ie.universityofgalway.groupnine.buildlogic.convention.configuration

import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.version
import ie.universityofgalway.groupnine.buildlogic.convention.library.CoverageExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.math.BigDecimal

internal fun Project.configureJacoco() {
    val ext = extensions.create<CoverageExtension>("coverage")

    extensions.getByType(JacocoPluginExtension::class).toolVersion = libs.version("jacoco-tool")

    tasks.withType(JacocoReport::class).configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }

    val report = tasks.named("jacocoTestReport", JacocoReport::class)

    val verify = tasks.named("jacocoTestCoverageVerification", JacocoCoverageVerification::class)

    tasks.withType(JacocoCoverageVerification::class).configureEach {
        doFirst {
            val min = BigDecimal(ext.minimum)

            violationRules.rule {
                ext.counters.forEach { counterName ->
                    limit {
                        counter = counterName
                        value = "COVEREDRATIO"
                        minimum = min
                    }
                }
            }
        }
    }

    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(verify)
    }

    tasks.findByName("test")?.let { test ->
        report.configure { dependsOn(test) }
        verify.configure { dependsOn(test) }
    }
}