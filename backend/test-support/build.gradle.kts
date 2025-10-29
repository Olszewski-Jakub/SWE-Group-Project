plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.spring.boot.dependencies)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
}

group = "ie.universityofgalway.groupnine.testsupport"
version = project.findProperty("version")!!

repositories { mavenCentral() }

dependencies {
    implementation(libs.jackson.databind)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.test)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.platform.engine)

}

tasks.test {
    useJUnitPlatform()
}
