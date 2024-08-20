group = "au.com.skater901.wc3connect.utilities"

repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin)
}

dependencies {
    implementation(libs.coroutines)
}

kotlin {
    explicitApi()
}