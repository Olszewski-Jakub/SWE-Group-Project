import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation

plugins {
    alias(libs.plugins.convention.security)
}

group = "ie.universityofgalway.groupnine.security"
version = project.findProperty("version")!!

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":service"))
    implementation(project(":util"))
}

coverage {
    minimum = 0.76
}
