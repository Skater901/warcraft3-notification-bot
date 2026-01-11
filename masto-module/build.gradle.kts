val jackson_version: String by project

group = "au.rakka.java.mastoapi"

repositories {
	mavenCentral()
}

plugins {
	alias(libs.plugins.kotlin)
}

dependencies {
	implementation(project(":module-api"))
	implementation(project(":extras"))
	implementation(project(":utilities"))
	implementation(libs.coroutines)
	implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
}

kotlin {
	explicitApi()
}