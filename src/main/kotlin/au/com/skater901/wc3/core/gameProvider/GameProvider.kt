package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.core.domain.Game
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.net.URI

internal interface GameProvider {
    val name: String

    val sourceURL: URI

    val gamesProvider: ObjectMapper.(InputStream) -> List<Game>
}