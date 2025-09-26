val discord_version: String by project
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

    implementation(project(":utilities"))

    // Discord API
    implementation("net.dv8tion:JDA:$discord_version")
    implementation("club.minnced:jda-ktx:$ktx_version")

    implementation(libs.guice) {
        exclude("com.google.guava", "guava")
    }
    implementation(libs.jdbi)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(project(":test-utilities"))

    testImplementation(libs.assertj)
    testImplementation(libs.mockito.kotlin)
}

tasks {
    compileJava {
        options.release = 24
    }

    test {
        useJUnitPlatform()
    }

    register<Test>("integrationTest") {
        group = "verification"

        useJUnitPlatform()

        dependsOn(test)

        filter {
            excludeTestsMatching("*Test")
            includeTestsMatching("*ITCase")
        }
    }

    jacocoTestReport {
        dependsOn("integrationTest")

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
