package au.com.skater901.w3cconnect.application.config

import au.com.skater901.w3cconnect.WC3ConnectDiscordNotificationBotConfiguration
import java.io.File
import java.util.*
import kotlin.reflect.KProperty

fun parseConfig(): WC3ConnectDiscordNotificationBotConfiguration {
    val configFilePath = System.getProperty("configFile")
        ?: throw IllegalArgumentException("Required system property [ configFile ] has not been set. Please set it, with a path to a config file, using -DconfigFile=/path/to/config/file.properties")

    // check file exists
    val configFile = File(configFilePath)
    if (!configFile.exists()) throw IllegalArgumentException("Config file [ $configFilePath ] does not exist.")

    val config = Properties().apply {
        configFile.inputStream().use { load(it) }
    }

    return WC3ConnectDiscordNotificationBotConfiguration.parse(config)
}

fun Properties.getPropertyOrThrow(prefix: String, property: KProperty<*>): String {
    val fullProperty = "$prefix.${property.name}"

    return getProperty(fullProperty)
        ?: throw IllegalStateException("No config property provided for [ $fullProperty ]")
}
