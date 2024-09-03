package au.com.skater901.wc3connect.core.gameProvider

import au.com.skater901.wc3connect.api.core.domain.Game
import au.com.skater901.wc3connect.application.config.WC3ConnectConfig
import au.com.skater901.wc3connect.core.domain.WC3ConnectGame
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Inject
import java.io.InputStream
import java.net.URI

internal class WC3ConnectGameProvider @Inject constructor(
    wC3ConnectConfig: WC3ConnectConfig
) : GameProvider {
    override val sourceURL: URI = wC3ConnectConfig.url
    override val gamesProvider: ObjectMapper.(InputStream) -> List<Game> = {
        readValue<List<WC3ConnectGame>>(it)
    }
}