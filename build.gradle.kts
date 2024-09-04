val mariadb_version: String by project
val jdbi_version: String by project
val hikari_version: String by project
val liquibase_version: String by project
val liquibase_logging_version: String by project
val logback_version: String by project
val classgraph_version: String by project
val resilience4j_version: String by project
val jackson_version: String by project

// Testing library versions
val mariadb_test_container_version: String by project
val wiremock_version: String by project
val wiremock_kotlin_version: String by project

plugins {
    alias(libs.plugins.kotlin)
    application

    jacoco
}

group = "au.com.skater901.wc3connect"
version = "0.3.0"

repositories {
    mavenCentral()
}

dependencies {
    // Core API library
    implementation(project(":module-api"))

    implementation(project(":utilities"))

    implementation(libs.kotlin.bom)
    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines)

    // Database libraries
    implementation("org.mariadb.jdbc:mariadb-java-client:$mariadb_version")
    implementation("org.jdbi:jdbi3-core:$jdbi_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")
    implementation("org.liquibase:liquibase-core:$liquibase_version")
    implementation("com.mattbertolini:liquibase-slf4j:$liquibase_logging_version")

    // Logging libraries
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // DI/reflection libraries
    implementation(libs.guice) {
        exclude("com.google.guava", "guava")
    }
    implementation(libs.guava)
    implementation("io.github.classgraph:classgraph:$classgraph_version")

    // Resilience Libraries
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4j_version")
    implementation("io.github.resilience4j:resilience4j-retry:$resilience4j_version")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4j_version")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")

    // Notification Modules
    implementation(project(":discord-module"))

    testImplementation(kotlin("test"))

    // Testing/assertion libraries
    testImplementation(libs.assertj)
    testImplementation(libs.mockito.kotlin)

    // Integration/end to end testing libraries
    testImplementation("org.testcontainers:mariadb:$mariadb_test_container_version")
    testImplementation("org.wiremock:wiremock:$wiremock_version")
    testImplementation("com.marcinziolo:kotlin-wiremock:$wiremock_kotlin_version")
}

jacoco {
    reportsDirectory = layout.buildDirectory.dir("coverage-reports")
}

tasks {
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

application {
    mainClass = "au.com.skater901.wc3connect.WC3ConnectNotificationBot"

    val configFile: String? by project

    applicationDefaultJvmArgs = listOfNotNull(configFile?.let { "-DconfigFile=$it" })
}
