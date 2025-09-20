plugins {
    alias(libs.plugins.convention.infrastructure)
}

group = "ie.universityofgalway.groupnine.infrastructure"
version = project.findProperty("version")!!

repositories {
    mavenCentral()
}

dependencies {
}

coverage {
    minimum = 0.75
}