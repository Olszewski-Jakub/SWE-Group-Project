package ie.universityofgalway.groupnine.buildlogic.convention.extensions

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.androidLibs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("androidLibs")

fun VersionCatalog.version(alias: String) = this.findVersion(alias).get().toString()
fun VersionCatalog.plugin(alias: String) = this.findPlugin(alias).get().get().pluginId
fun VersionCatalog.library(alias: String) = this.findLibrary(alias).get()
fun VersionCatalog.bundle(alias: String) = this.findBundle(alias).get().get()