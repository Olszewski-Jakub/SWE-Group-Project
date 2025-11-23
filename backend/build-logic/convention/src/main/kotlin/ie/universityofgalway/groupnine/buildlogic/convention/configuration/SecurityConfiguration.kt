package ie.universityofgalway.groupnine.buildlogic.convention.configuration

import ie.universityofgalway.groupnine.buildlogic.convention.extensions.compileOnly
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.library
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.libs
import ie.universityofgalway.groupnine.buildlogic.convention.extensions.testImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureSecurity() {
    dependencies {
        compileOnly(libs.library("jakarta-servlet-api"))
        
        implementation(libs.library("jackson-databind"))
        implementation(libs.library("spring-boot-configuration-processor"))
        implementation(libs.library("spring-boot-starter"))
        implementation(libs.library("spring-boot-starter-oauth2-resource-server"))
        implementation(libs.library("spring-boot-starter-security"))
        implementation(libs.library("spring-boot-starter-validation"))
        implementation(libs.library("spring-boot-starter-web"))
        implementation(libs.library("spring-context"))
        implementation(libs.library("spring-security-oauth2-jose"))

        testImplementation(libs.library("assertj-core"))
        testImplementation(libs.library("jakarta-servlet-api"))
    }
}
