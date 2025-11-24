package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.annotation.ClientFor
import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.core.domain.WC3ConnectGame
import jakarta.inject.Inject
import jakarta.ws.rs.client.AsyncInvoker
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.WebTarget
import jakarta.ws.rs.core.GenericType
import java.util.concurrent.Future

internal class WC3ConnectGameProvider @Inject constructor(
    @param:ClientFor("wc3connect")
    private val client: Client
) : GameProvider {
    override fun webTarget(): WebTarget = client.target("/allgames")

    override val getGames: AsyncInvoker.() -> Future<out List<Game>> = {
        get(object : GenericType<List<WC3ConnectGame>>() {})
    }
}