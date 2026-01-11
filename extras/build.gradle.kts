val jersey_version: String by project

group = "au.com.skater901.wc3.extras"

repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin)
}

dependencies {
    api(libs.guice)

    api("jakarta.ws.rs:jakarta.ws.rs-api:${jersey_version}")
}

kotlin {
    explicitApi()
}