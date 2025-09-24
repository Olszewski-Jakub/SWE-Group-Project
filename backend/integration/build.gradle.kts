plugins {
    alias(libs.plugins.convention.integration)
}

group = "ie.universityofgalway.groupnine.integration"
version = project.findProperty("version")!!

dependencies {
    implementation(libs.flywaydb.core)
    implementation(libs.flywaydb.postgres)
}

coverage {
    minimum = 0.33
}