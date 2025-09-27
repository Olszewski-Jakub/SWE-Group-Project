plugins {
    id("java-library")
    alias(libs.plugins.convention.testing)
    alias(libs.plugins.convention.jacoco)
}

group = "ie.universityofgalway.groupnine.util"
version = project.findProperty("version")!!

repositories {
    mavenCentral()
}

dependencies {
    api(libs.slf4j.api)
}

coverage {
    minimum = 0.5
}
