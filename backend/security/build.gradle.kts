plugins {
    alias(libs.plugins.convention.security)
}

group = "ie.universityofgalway.groupnine.security"
version = project.findProperty("version")!!

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.security.oauth2.jose)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.web)
    implementation(project(":domain"))
}

coverage {
    minimum = 0.35
}
