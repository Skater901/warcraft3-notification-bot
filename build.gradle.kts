val mysql_version: String by project
val jdbi_version: String by project
val hikari_version: String by project
val liquibase_version: String by project
val liquibase_logging_version: String by project
val logback_version: String by project
val resilience4j_version: String by project
val jackson_version: String by project

// Testing library versions
val assertj_version: String by project
val mockito_version: String by project
val mysql_test_container_version: String by project
val wiremock_version: String by project
val wiremock_kotlin_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    jacoco
}

group = "au.com.skater901.wc3connect"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.coroutines)

    // Database libraries
    implementation("com.mysql:mysql-connector-j:$mysql_version")
    implementation("org.jdbi:jdbi3-core:$jdbi_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")
    implementation("org.liquibase:liquibase-core:$liquibase_version")
    implementation("com.mattbertolini:liquibase-slf4j:$liquibase_logging_version")

    // Logging libraries
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation(libs.guice)

    // Resilience Libraries
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4j_version")
    implementation("io.github.resilience4j:resilience4j-retry:$resilience4j_version")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4j_version")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")

    // Notification Modules
    runtimeOnly(project(":discord-module")) // TODO make run depend on compile

    testImplementation(kotlin("test"))

    // Testing/assertion libraries
    testImplementation("org.assertj:assertj-core:$assertj_version")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockito_version")

    // Integration/end to end testing libraries
    testImplementation("org.testcontainers:mysql:$mysql_test_container_version")
    testImplementation("org.wiremock:wiremock:$wiremock_version")
    testImplementation("com.marcinziolo:kotlin-wiremock:$wiremock_kotlin_version")
}

sourceSets {
    main {
        kotlin { srcDir("src/main/kotlin") }
        resources { srcDir("src/main/resources") }
    }

    test {
        kotlin { srcDir("src/test/kotlin") }
        resources { srcDir("src/test/resources") }
    }
}

jacoco {
    reportsDirectory = file("$buildDir/coverage-reports")
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE // TODO review this
    }

    test {
        useJUnitPlatform()
    }

    jacocoTestReport {
        dependsOn(test)
    }

    jacocoTestCoverageVerification {
        dependsOn(jacocoTestReport)
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal("0.9")
                }
            }
        }
    }
}

kotlin {
    jvmToolchain(21)

    explicitApi()
}