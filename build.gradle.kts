val discord_version: String by project
val ktx_version: String by project
val coroutines_version: String by project
val mysql_version: String by project
val jdbi_version: String by project
val hikari_version: String by project
val liquibase_version: String by project
val liquibase_logging_version: String by project
val logback_version: String by project
val guice_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
}

group = "au.com.skater901"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Discord API
    implementation("net.dv8tion:JDA:$discord_version")
    implementation("club.minnced:jda-ktx:$ktx_version")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")

    // Database libraries
    implementation("com.mysql:mysql-connector-j:$mysql_version")
    implementation("org.jdbi:jdbi3-core:$jdbi_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")
    implementation("org.liquibase:liquibase-core:$liquibase_version")
    implementation("com.mattbertolini:liquibase-slf4j:$liquibase_logging_version")

    // Logging libraries
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("com.google.inject:guice:$guice_version")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}