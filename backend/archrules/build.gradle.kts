plugins {
    alias(libs.plugins.convention.testing)
    alias(libs.plugins.convention.jacoco)
    alias(libs.plugins.java.library)
}

group = "ie.universityofgalway.groupnine.archrules"
version = project.findProperty("version")!!

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories { mavenCentral() }

dependencies {
    testImplementation(libs.archunit.junit5.api)
    testImplementation(libs.archunit.core)
    testImplementation(libs.spring.boot.starter.web)

    testImplementation(project(":domain"))
    testImplementation(project(":service"))
    testImplementation(project(":infrastructure"))
    testImplementation(project(":delivery:rest"))
    testImplementation(project(":security"))
    testImplementation(project(":integration"))
}

coverage {
    minimum = 0.05
}