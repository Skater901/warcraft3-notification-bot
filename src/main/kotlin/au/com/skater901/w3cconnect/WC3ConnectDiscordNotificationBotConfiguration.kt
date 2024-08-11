package au.com.skater901.w3cconnect

import au.com.skater901.w3cconnect.application.config.DatabaseConfig
import au.com.skater901.w3cconnect.application.config.DiscordConfiguration
import au.com.skater901.w3cconnect.application.config.GamesConfiguration
import au.com.skater901.w3cconnect.application.config.LogConfiguration
import java.util.*

class WC3ConnectDiscordNotificationBotConfiguration(
    val discordConfiguration: DiscordConfiguration,
    val gamesConfiguration: GamesConfiguration,
    val logging: LogConfiguration,
    val database: DatabaseConfig
) {
    companion object {
        // TODO consider using an actual library for parsing config (instead of this nonsense hacked together solution)
        fun parse(properties: Properties) = WC3ConnectDiscordNotificationBotConfiguration(
            DiscordConfiguration.parse(
                WC3ConnectDiscordNotificationBotConfiguration::discordConfiguration.name,
                properties
            ),
            GamesConfiguration.parse(
                WC3ConnectDiscordNotificationBotConfiguration::gamesConfiguration.name,
                properties
            ),
            LogConfiguration.parse(WC3ConnectDiscordNotificationBotConfiguration::logging.name, properties),
            DatabaseConfig.parse(WC3ConnectDiscordNotificationBotConfiguration::database.name, properties)
        )
    }
}