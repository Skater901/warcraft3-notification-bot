val liquibase_version: String by project
val test_containers_version: String by project

group = "au.com.skater901.wc3.test.utilities"

repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin)
}

dependencies {
    implementation(libs.jdbi)
    api(libs.junit)
    implementation(libs.drivers.mariadb)
    implementation(libs.drivers.mysql)
    implementation(project(":utilities"))
    implementation("org.liquibase:liquibase-core:$liquibase_version")

    api("org.testcontainers:testcontainers-mysql:$test_containers_version")
    api("org.testcontainers:testcontainers-mariadb:$test_containers_version")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

kotlin {
    explicitApi()
}