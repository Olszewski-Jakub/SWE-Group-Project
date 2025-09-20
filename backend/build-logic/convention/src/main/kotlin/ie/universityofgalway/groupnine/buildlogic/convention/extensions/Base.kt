package ie.universityofgalway.groupnine.buildlogic.convention.extensions

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

internal fun Project.configureJava17ToolchainIfPresent() {
    plugins.withType(JavaPlugin::class.java) {
        extensions.configure(JavaPluginExtension::class.java) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(libs.version("java")))
        }
    }
}

internal fun Project.applyIfMissing(vararg ids: String) {
    ids.forEach { id ->
        if (!plugins.hasPlugin(id)) {
            plugins.apply(id)
        }
    }
}
