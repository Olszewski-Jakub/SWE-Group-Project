plugins {
    alias(libs.plugins.convention.service)
}

group = "ie.universityofgalway.groupnine.service"
version = project.findProperty("version")!!

dependencies {
}

coverage {
    minimum = 0.6
}