plugins {
    id("java")
    alias(libs.plugins.convention.testing)
    alias(libs.plugins.convention.jacoco)
}

group = "ie.universityofgalway.groupnine.delivery.worker"

dependencies {
    implementation(project(":domain"))
    implementation(project(":infrastructure"))
    implementation(project(":service"))
    implementation(project(":util"))

    implementation(libs.jackson.databind)
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.context)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(project(":test-support"))
}

coverage {
    minimum = 0.69
}
