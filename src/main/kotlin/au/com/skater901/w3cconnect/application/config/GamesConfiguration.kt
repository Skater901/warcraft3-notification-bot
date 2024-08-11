package au.com.skater901.w3cconnect.application.config

import java.net.URI
import java.net.URISyntaxException
import java.util.*

class GamesConfiguration(
    val gamesURL: URI,
    val refreshInterval: Long
) {
    companion object {
        fun parse(prefix: String, properties: Properties): GamesConfiguration = GamesConfiguration(
            properties.getPropertyOrThrow(prefix, GamesConfiguration::gamesURL)
                .let {
                    try {
                        URI(it)
                    } catch (e: URISyntaxException) {
                        throw IllegalStateException(
                            "[ $prefix.${GamesConfiguration::gamesURL.name} ] had invalid URL for fetching games: ${e.message}. Provided value was [ ${
                                properties.getPropertyOrThrow(
                                    prefix,
                                    GamesConfiguration::gamesURL
                                )
                            } ]"
                        )
                    }
                },
            try {
                properties.getPropertyOrThrow(prefix, GamesConfiguration::refreshInterval)
                    .toLong()
            } catch (e: NumberFormatException) {
                throw IllegalStateException(
                    "[ $prefix.${GamesConfiguration::refreshInterval.name} ] value provided was not a number, provided value was [ ${
                        properties.getPropertyOrThrow(
                            prefix,
                            GamesConfiguration::refreshInterval
                        )
                    } ]"
                )
            }
        )
    }
}