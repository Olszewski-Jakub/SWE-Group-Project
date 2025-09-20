plugins {
    alias(libs.plugins.convention.domain)
}

group = "ie.universityofgalway.groupnine"
version = project.findProperty("version")!!

dependencies {
}

coverage {
    minimum = 0.75
}