plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "wc3-notification-bot"

// API for modules to implement
include("module-api")

// Modules
include("discord-module")
include("masto-module")

// Utilities
include("utilities")
include("test-utilities")
