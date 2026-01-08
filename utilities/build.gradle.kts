val metrics_version: String by project
val resilience4j_version: String by project

group = "au.com.skater901.wc3.utilities"

repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin)
}

dependencies {
    api(libs.coroutines)
    api(libs.guice)

    api(libs.jdbi)

    implementation("io.dropwizard.metrics:metrics-core:$metrics_version")

    // Resilience Libraries
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4j_version")
    api("io.github.resilience4j:resilience4j-retry:$resilience4j_version")
    api("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4j_version")
    implementation("io.github.resilience4j:resilience4j-metrics:$resilience4j_version")
}

kotlin {
    explicitApi()
}