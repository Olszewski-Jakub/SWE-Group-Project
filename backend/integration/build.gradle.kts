plugins {
    alias(libs.plugins.convention.integration)
}

group = "ie.universityofgalway.groupnine.integration"
version = project.findProperty("version")!!

dependencies {
    implementation(libs.flywaydb.core)
    implementation(libs.flywaydb.postgres)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.security.crypto)
    implementation(project(":security"))
    implementation(libs.spring.boot.starter.data.redis)
    implementation(project(":delivery:worker"))
    implementation(libs.spring.boot.starter.amqp)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(platform(libs.junit.bom))
    testImplementation("com.h2database:h2:2.2.224")
}

coverage {
    minimum = 0.91
}
