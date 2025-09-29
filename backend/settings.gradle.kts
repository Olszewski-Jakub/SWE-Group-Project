// settings.gradle.kts
pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "backend"
include("integration")

include("domain")
include("service")
include("infrastructure")
include("delivery:rest")
include("delivery:worker")
include("test-support")
include("security")
include("archrules")
include("util")
