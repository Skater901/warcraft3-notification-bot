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

    // Resilience Libraries
    api("io.github.resilience4j:resilience4j-kotlin:$resilience4j_version")
    api("io.github.resilience4j:resilience4j-retry:$resilience4j_version")
    api("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4j_version")
}

tasks {
    compileJava {
        options.release = 24
    }
}

kotlin {
    explicitApi()
}