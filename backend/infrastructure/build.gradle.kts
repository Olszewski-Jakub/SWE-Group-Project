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
    implementation("org.springframework.security:spring-security-crypto")
}

coverage {
    minimum = 0.75
}
