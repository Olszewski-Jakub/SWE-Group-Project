import ie.universityofgalway.groupnine.buildlogic.convention.extensions.testImplementation

plugins {
    id("java")
    alias(libs.plugins.convention.testing)
    alias(libs.plugins.convention.jacoco)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":security"))
    implementation(project(":service"))
    implementation(project(":util"))

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    testImplementation(project(":test-support"))
    testImplementation(libs.jackson.databind)
    testImplementation(libs.spring.boot.starter.test)
}

coverage {
    minimum = 0.47
}
