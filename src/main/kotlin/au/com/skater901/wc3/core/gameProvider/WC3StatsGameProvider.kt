package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.annotation.ClientFor
import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.core.domain.WC3StatsGame
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.inject.Inject
import jakarta.ws.rs.client.AsyncInvoker
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.WebTarget
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

internal class WC3StatsGameProvider @Inject constructor(
    @param:ClientFor("wc3stats")
    private val client: Client
) : GameProvider {
    override fun webTarget(): WebTarget = client.target("/gamelist")

    override val getGames: AsyncInvoker.() -> Future<out List<Game>> = {
        // Dirty hack because Futures are very miserable to work with, and I know that Jersey is using CompletableFutures
        (get(Games::class.java) as CompletableFuture<Games>).thenApply { it.body }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Games(val body: List<WC3StatsGame>)
}