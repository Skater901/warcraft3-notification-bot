package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.application.config.WC3ConnectConfig
import au.com.skater901.wc3.core.domain.WC3ConnectGame
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Inject
import java.io.InputStream
import java.net.URI

internal class WC3ConnectGameProvider @Inject constructor(
    wc3ConnectConfig: WC3ConnectConfig
) : GameProvider {
    override val sourceURL: URI = wc3ConnectConfig.url
    override val gamesProvider: ObjectMapper.(InputStream) -> List<Game> = {
        readValue<List<WC3ConnectGame>>(it)
    }
}