plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "wc3-notification-bot"

// API for modules to implement
include("module-api")

// Extras
include("extras")

// Modules
include("discord-module")
include("masto-module")

// Utilities
include("utilities")
include("test-utilities")
