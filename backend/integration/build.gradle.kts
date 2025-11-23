import ie.universityofgalway.groupnine.buildlogic.convention.extensions.implementation

plugins {
    alias(libs.plugins.convention.integration)
}

group = "ie.universityofgalway.groupnine.integration"
version = project.findProperty("version")!!

dependencies {
    implementation(project(":delivery:rest"))
    implementation(project(":delivery:webhook"))
    implementation(project(":delivery:worker"))
    implementation(project(":domain"))
    implementation(project(":infrastructure"))
    implementation(project(":security"))
    implementation(project(":security"))
    implementation(project(":service"))
    implementation(project(":util"))
}

coverage {
    minimum = 0.50
}
