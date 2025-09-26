val test_containers_version: String by project

group = "au.com.skater901.wc3.test.utilities"

repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin)
}

dependencies {
    api(libs.jdbi)
    api(libs.junit)
    implementation(libs.drivers.mariadb)
    implementation(libs.drivers.mysql)
    implementation(project(":utilities"))
    implementation(libs.liquibase)

    api("org.testcontainers:mysql:$test_containers_version")
    api("org.testcontainers:mariadb:$test_containers_version")
}

tasks {
    compileJava {
        options.release = 24
    }

    test {
        useJUnitPlatform()
    }
}

kotlin {
    explicitApi()
}