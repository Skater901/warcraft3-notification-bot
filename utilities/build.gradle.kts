group = "au.com.skater901.wc3.utilities"

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