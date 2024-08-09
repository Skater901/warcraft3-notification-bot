package au.com.skater901.w3cconnect.application.config

import java.util.*

class DiscordConfiguration(
    val publicKey: String,
    val privateToken: String
) {
    companion object {
        fun parse(prefix: String, properties: Properties): DiscordConfiguration = DiscordConfiguration(
            properties.getPropertyOrThrow(prefix, DiscordConfiguration::publicKey),
            properties.getPropertyOrThrow(prefix, DiscordConfiguration::privateToken)
        )
    }
}