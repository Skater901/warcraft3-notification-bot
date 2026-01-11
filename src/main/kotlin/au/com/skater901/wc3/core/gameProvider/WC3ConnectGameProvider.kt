package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.application.healthcheck.WC3ConnectHealthCheck
import au.com.skater901.wc3.core.domain.WC3ConnectGame
import au.com.skater901.wc3.extras.annotation.ClientFor
import au.com.skater901.wc3.utilities.coroutines.await
import au.com.skater901.wc3.utilities.defaultUnitOfWork
import jakarta.inject.Inject
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.core.GenericType
import jakarta.ws.rs.core.MediaType

internal class WC3ConnectGameProvider @Inject constructor(
    @param:ClientFor("wc3connect")
    private val client: Client
) : GameProvider {
    private val getGamesWork = defaultUnitOfWork(::getGames)
    override suspend fun getGames(): List<Game> = try {
        getGamesWork {
            client.target("/allgames")
                .request(MediaType.APPLICATION_JSON)
                .async()
                .get(object : GenericType<List<WC3ConnectGame>>() {})
                .await()
                .also {
                    if (it.isEmpty()) {
                        WC3ConnectHealthCheck.noGames()
                    } else {
                        WC3ConnectHealthCheck.healthy()
                    }
                }
        }
    } catch (e: Exception) {
        WC3ConnectHealthCheck.exception(e)
        throw e
    }
}