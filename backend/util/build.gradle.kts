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
    api("org.slf4j:slf4j-api:2.0.13")
}

coverage {
    minimum = 0.5
}

