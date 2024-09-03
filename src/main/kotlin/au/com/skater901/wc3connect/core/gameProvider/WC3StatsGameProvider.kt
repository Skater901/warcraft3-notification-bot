package au.com.skater901.wc3connect.core.gameProvider

import au.com.skater901.wc3connect.api.core.domain.Game
import au.com.skater901.wc3connect.application.config.WC3StatsConfig
import au.com.skater901.wc3connect.core.domain.WC3StatsGame
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Inject
import java.io.InputStream
import java.net.URI

internal class WC3StatsGameProvider @Inject constructor(
    wc3StatsConfig: WC3StatsConfig
) : GameProvider {
    override val sourceURL: URI = wc3StatsConfig.url
    override val gamesProvider: ObjectMapper.(InputStream) -> List<Game> = {
        readValue<Games>(it).body
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Games(val body: List<WC3StatsGame>)
}