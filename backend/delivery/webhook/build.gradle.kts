plugins {
    id("java")
    alias(libs.plugins.convention.testing)
    alias(libs.plugins.convention.jacoco)
}

group = "ie.universityofgalway.groupnine.delivery.webhook"
version = project.findProperty("version")!!

dependencies {
    implementation(project(":domain"))
    implementation(project(":service"))
    implementation(project(":util"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.stripe.java)

    testImplementation(libs.spring.boot.starter.test)
}

coverage {
    minimum = 0.99
}

