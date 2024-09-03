val discord_version: String by project
val ktx_version: String by project

group = "au.com.skater901.wc3connect.discord"

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

    testImplementation(kotlin("test"))

    testImplementation(libs.assertj)
    testImplementation(libs.mockito.kotlin)
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
                    minimum = BigDecimal("0.5")
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
