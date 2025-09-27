plugins {
    alias(libs.plugins.convention.integration)
}

group = "ie.universityofgalway.groupnine.integration"
version = project.findProperty("version")!!

dependencies {
    implementation(libs.flywaydb.core)
    implementation(libs.flywaydb.postgres)
    implementation(libs.spring.boot.starter.security)
    implementation("org.springframework.security:spring-security-crypto")

}

coverage {
    minimum = 0.33
}