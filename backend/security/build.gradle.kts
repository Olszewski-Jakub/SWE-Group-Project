plugins {
    alias(libs.plugins.convention.security)
}

group = "ie.universityofgalway.groupnine.security"
version = project.findProperty("version")!!

repositories {
    mavenCentral()
}

dependencies {

}

coverage {
    minimum = 0.35
}