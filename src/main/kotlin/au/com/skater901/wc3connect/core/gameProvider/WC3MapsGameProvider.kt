package au.com.skater901.wc3connect.core.gameProvider

import au.com.skater901.wc3connect.api.core.domain.Game
import au.com.skater901.wc3connect.application.config.WC3MapsConfig
import au.com.skater901.wc3connect.core.domain.WC3MapsGame
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Inject
import java.io.InputStream
import java.net.URI

internal class WC3MapsGameProvider @Inject constructor(
    wc3MapsConfig: WC3MapsConfig
) : GameProvider {
    override val sourceURL: URI = wc3MapsConfig.url
    override val gamesProvider: ObjectMapper.(InputStream) -> List<Game> = {
        readValue<Games>(it).results
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Games(val results: List<WC3MapsGame>)
}