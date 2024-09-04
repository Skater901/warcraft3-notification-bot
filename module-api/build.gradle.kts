group = "au.com.skater901.wc3.api"

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