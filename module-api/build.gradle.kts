val jersey_version: String by project
val metrics_version: String by project

group = "au.com.skater901.wc3.api"

repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin)
}

dependencies {
    api(libs.guice)
    api("jakarta.ws.rs:jakarta.ws.rs-api:$jersey_version")

    api("io.dropwizard.metrics:metrics-healthchecks:$metrics_version")
}

kotlin {
    explicitApi()
}