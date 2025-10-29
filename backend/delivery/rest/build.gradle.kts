import ie.universityofgalway.groupnine.buildlogic.convention.extensions.testImplementation

plugins {
    id("java")
    alias(libs.plugins.convention.testing)
    alias(libs.plugins.convention.jacoco)
}

dependencies {
    
    implementation(project(":service"))
    implementation(project(":domain"))
    implementation(project(":security"))
    implementation(libs.spring.boot.starter.security)
    implementation(project(":util"))
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    implementation(libs.spring.boot.starter.data.jpa) 
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.jackson.databind)
    testImplementation(project(":test-support"))
}

coverage {
    minimum = 0.47
}
