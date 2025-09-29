plugins {
    alias(libs.plugins.convention.infrastructure)
}

group = "ie.universityofgalway.groupnine.infrastructure"
version = project.findProperty("version")!!

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.security.crypto)
    implementation(project(":security"))
    implementation(project(":service"))
    implementation(project(":domain"))
    implementation(libs.jackson.databind)
    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.thymeleaf)
    // Mailjet API client
    implementation(libs.mailjet.client)
}

coverage {
    minimum = 0.6
}
