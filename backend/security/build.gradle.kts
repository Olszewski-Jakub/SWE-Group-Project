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
}

coverage {
    minimum = 0.35
}
