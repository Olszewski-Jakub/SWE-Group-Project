plugins {
    id("java")
    `kotlin-dsl`
}

group = "ie.universityofgalway.groupnine.buildlogic.convention"
version = project.findProperty("version")!!

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-gradle-plugin:3.3.4")
    compileOnly("io.spring.gradle:dependency-management-plugin:1.1.6")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20")
    compileOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.2.0-2.0.2")
    compileOnly(gradleApi())
}

gradlePlugin {
    plugins {
        register("conventionTesting") {
            id = "ie.universityofgalway.groupnine.library.testing"
            implementationClass = "ie.universityofgalway.groupnine.buildlogic.convention.library.TestingConventionPlugin"
            displayName = "Testing Convention"
            description = "JUnit 5, Kotlin tests (optional), common test logging"
        }
        register("conventionJacoco") {
            id = "ie.universityofgalway.groupnine.library.jacoco"
            implementationClass = "ie.universityofgalway.groupnine.buildlogic.convention.library.JacocoConventionPlugin"
            displayName = "JaCoCo Convention"
            description = "Per-module coverage with a simple coverage { } DSL"
        }
        register("conventionDomain") {
            id = "ie.universityofgalway.groupnine.library.domain"
            implementationClass = "ie.universityofgalway.groupnine.buildlogic.convention.library.DomainConventionPlugin"
        }
        register("conventionInfrastructure") {
            id = "ie.universityofgalway.groupnine.library.infrastructure"
            implementationClass = "ie.universityofgalway.groupnine.buildlogic.convention.library.InfrastructureConventionPlugin"
        }
        register("conventionSecurity") {
            id = "ie.universityofgalway.groupnine.library.security"
            implementationClass = "ie.universityofgalway.groupnine.buildlogic.convention.library.SecurityConventionPlugin"
        }
        register("conventionIntegration") {
            id = "ie.universityofgalway.groupnine.library.integration"
            implementationClass = "ie.universityofgalway.groupnine.buildlogic.convention.library.IntegrationConventionPlugin"
        }
        register("conventionService") {
            id = "ie.universityofgalway.groupnine.library.service"
            implementationClass = "ie.universityofgalway.groupnine.buildlogic.convention.library.ServiceConventionPlugin"
        }
    }
}