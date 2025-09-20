plugins {
    alias(libs.plugins.convention.integration)
}

group = "ie.universityofgalway.groupnine.integration"
version = project.findProperty("version")!!

dependencies {

}

coverage {
    minimum = 0.33
}