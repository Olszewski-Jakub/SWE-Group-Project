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
    implementation(libs.spring.security.crypto)
    implementation(project(":security"))
}

coverage {
    minimum = 0.6
}
