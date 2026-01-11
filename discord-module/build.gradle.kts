val jda_version: String by project
val ktx_version: String by project

group = "au.com.skater901.wc3.discord"

repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.kotlin)
    jacoco
}

dependencies {
    implementation(project(":module-api"))

    implementation(project(":extras"))
    implementation(project(":utilities"))

    // Discord API
    implementation("net.dv8tion:JDA:$jda_version")
    implementation("club.minnced:jda-ktx:$ktx_version")

    implementation(libs.jdbi)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(project(":test-utilities"))

    testImplementation(libs.assertj)
    testImplementation(libs.mockito.kotlin)
}

tasks {
    test {
        useJUnitPlatform()
    }

    register<Test>("integrationTest") {
        group = "verification"

        useJUnitPlatform()

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
                    minimum = BigDecimal("0.6")
                }
            }
        }
    }
}

jacoco {
    reportsDirectory = layout.buildDirectory.dir("coverage-reports")
}

kotlin {
    explicitApi()
}
