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
    implementation(project(":"))

    // Discord API
    implementation("net.dv8tion:JDA:$discord_version")
    implementation("club.minnced:jda-ktx:$ktx_version")

    implementation(libs.guice)

    testImplementation(kotlin("test"))

    testImplementation(libs.mockito.kotlin)
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

tasks {
    test {
        useJUnitPlatform()
    }
}

kotlin {
    explicitApi()
}
