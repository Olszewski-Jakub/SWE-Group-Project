plugins {
    alias(libs.plugins.convention.infrastructure)
}

group = "ie.universityofgalway.groupnine.infrastructure"
version = project.findProperty("version")!!

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":security"))
    implementation(project(":service"))

    implementation(libs.jackson.databind)
    implementation(libs.mailjet.client)
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.spring.security.crypto)
    implementation(libs.stripe.java)
    implementation(libs.thymeleaf)
}

coverage {
    minimum = 0.75
}
