val dropwizard_version: String by project
val jackson_version: String by project
val logback_version: String by project

val classgraph_version: String by project
val dropwizard_guicey_version: String by project

val hikari_version: String by project
val liquibase_slf4j_version: String by project

// Testing library versions
val wiremock_version: String by project
val wiremock_kotlin_version: String by project
val test_containers_version: String by project

plugins {
    alias(libs.plugins.kotlin)
    application

    jacoco
}

group = "au.com.skater901.wc3"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Core API library
    implementation(project(":module-api"))

    // Dropwizard
    implementation(platform("io.dropwizard:dropwizard-bom:$dropwizard_version"))
    implementation("io.dropwizard:dropwizard-client")
    implementation("io.dropwizard:dropwizard-core")
    implementation("io.dropwizard:dropwizard-jdbi3")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jackson_version}")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.coroutines)

    // Database libraries
    implementation(libs.drivers.mysql)
    implementation(libs.drivers.mariadb)
    implementation("com.zaxxer:HikariCP:$hikari_version")
    implementation(libs.liquibase)
    implementation("com.mattbertolini:liquibase-slf4j:$liquibase_slf4j_version")

    // DI/reflection libraries
    implementation("io.github.classgraph:classgraph:$classgraph_version")
    implementation("ru.vyarus:dropwizard-guicey:$dropwizard_guicey_version")

    implementation(project(":utilities"))

    // Notification Modules
    implementation(project(":discord-module"))
    implementation(project(":masto-module"))

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)

    // Testing/assertion libraries
    testImplementation(libs.assertj)
    testImplementation(libs.mockito.kotlin)

    // Integration/end-to-end testing libraries
    testImplementation("org.wiremock:wiremock-jetty12:$wiremock_version")
    testImplementation("com.marcinziolo:kotlin-wiremock:$wiremock_kotlin_version")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:$test_containers_version")
    testImplementation("io.dropwizard:dropwizard-testing")

    testImplementation(project(":test-utilities"))
}

jacoco {
    reportsDirectory = layout.buildDirectory.dir("coverage-reports")
}

tasks {
    test {
        useJUnitPlatform()

        filter {
            excludeTestsMatching("*ITCase")
        }
    }

    register<Test>("integrationTest") {
        group = "verification"

        useJUnitPlatform()

        testClassesDirs = test.get().testClassesDirs
        classpath = test.get().classpath

        shouldRunAfter(test)

        filter {
            excludeTestsMatching("*Test")
            includeTestsMatching("*ITCase")
        }
    }

    jacocoTestReport {
        dependsOn(test, "integrationTest")

        executionData(test.get(), named("integrationTest").get())
    }

    jacocoTestCoverageVerification {
        dependsOn(jacocoTestReport)

        executionData(test.get(), named("integrationTest").get())

        violationRules {
            rule {
                limit {
                    minimum = BigDecimal("0.89")
                }
            }
        }
    }

    wrapper {
        gradleVersion = "9.2.1"
        distributionType = Wrapper.DistributionType.ALL
    }

    check {
        dependsOn("integrationTest")
    }
}

kotlin {
    explicitApi()
}

application {
    mainClass = "au.com.skater901.wc3.WC3NotificationBot"

    val configFile: String? by project
    val enabledModules: String? by project

    applicationDefaultJvmArgs = listOfNotNull(
        configFile?.let { "-DconfigFile=$it" },
        "-DappVersion=${project.version}",
        enabledModules?.let { "-DenabledModules=$it" }
    )
}
