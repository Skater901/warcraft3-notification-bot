package au.com.skater901.wc3connect.core.gameProvider

import au.com.skater901.wc3connect.api.core.domain.Game
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.net.URI

internal interface GameProvider {
    val sourceURL: URI

    val gamesProvider: ObjectMapper.(InputStream) -> List<Game>
}