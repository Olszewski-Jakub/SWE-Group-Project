plugins {
    alias(libs.plugins.convention.service)
}

group = "ie.universityofgalway.groupnine.service"
version = project.findProperty("version")!!

dependencies {
    implementation(libs.jackson.databind)
    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-commons")
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.4")) 
    implementation(project(":domain"))
    implementation(project(":util"))
}

coverage {
    minimum = 0.75
}
