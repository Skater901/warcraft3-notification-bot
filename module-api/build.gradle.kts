group = "au.com.skater901.wc3connect.api"

repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin)
}

dependencies {
    implementation(libs.guice) {
        exclude("com.google.guava", "guava")
    }
}

kotlin {
    explicitApi()
}