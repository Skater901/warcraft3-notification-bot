package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.core.domain.Game
import jakarta.ws.rs.client.AsyncInvoker
import jakarta.ws.rs.client.WebTarget
import java.util.concurrent.Future

internal interface GameProvider {
    fun webTarget(): WebTarget

    val getGames: AsyncInvoker.() -> Future<out List<Game>>
}