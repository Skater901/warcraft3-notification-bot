package au.com.skater901.w3cconnect

import au.com.skater901.w3cconnect.application.config.DatabaseConfig
import au.com.skater901.w3cconnect.application.config.DiscordConfiguration
import java.util.*

class WC3ConnectDiscordNotificationBotConfiguration(
    val discordConfiguration: DiscordConfiguration,
    val database: DatabaseConfig
) {
    companion object {
        // TODO consider using an actual library for parsing config (instead of this nonsense hacked together solution)
        fun parse(properties: Properties) = WC3ConnectDiscordNotificationBotConfiguration(
            DiscordConfiguration.parse("discordConfiguration", properties),
            DatabaseConfig.parse("database", properties)
        )
    }
}