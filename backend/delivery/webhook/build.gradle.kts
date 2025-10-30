plugins {
    id("java")
    alias(libs.plugins.convention.testing)
    alias(libs.plugins.convention.jacoco)
}

group = "ie.universityofgalway.groupnine.delivery.webhook"
version = project.findProperty("version")!!

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(project(":service"))
    implementation(project(":domain"))
    implementation(project(":util"))
    implementation("com.stripe:stripe-java:24.9.0")
    testImplementation(libs.spring.boot.starter.test)
}

coverage {
    minimum = 0.99
}

