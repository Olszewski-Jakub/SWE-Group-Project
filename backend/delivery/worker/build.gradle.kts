plugins {
    id("java")
    alias(libs.plugins.convention.testing)
    alias(libs.plugins.convention.jacoco)
}

group = "ie.universityofgalway.groupnine.delivery.worker"

dependencies {
    implementation(project(":service"))
    implementation(project(":domain"))
    implementation(project(":infrastructure"))

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.context)
    implementation(libs.jackson.databind)
    implementation(libs.spring.boot.starter.validation)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(project(":test-support"))
}

coverage {
    minimum = 0.2
}
