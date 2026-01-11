package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.application.healthcheck.GameSourceHealthCheck
import au.com.skater901.wc3.application.healthcheck.WC3MapsHealthCheck
import au.com.skater901.wc3.application.healthcheck.WC3StatsHealthCheck
import au.com.skater901.wc3.core.domain.WC3MapsGame
import au.com.skater901.wc3.core.domain.WC3StatsGame
import au.com.skater901.wc3.extras.annotation.ClientFor
import au.com.skater901.wc3.utilities.coroutines.await
import au.com.skater901.wc3.utilities.defaultUnitOfWork
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.inject.Inject
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.core.MediaType
import org.slf4j.LoggerFactory

internal class BattleNetGameProvider @Inject constructor(
    @param:ClientFor("wc3stats")
    private val wc3StatsClient: Client,
    @param:ClientFor("wc3maps")
    private val wc3MapsClient: Client
) : GameProvider {
    companion object {
        private val logger = LoggerFactory.getLogger(BattleNetGameProvider::class.java)
    }

    private val wc3StatsGamesWork = defaultUnitOfWork("wc3StatsGames")
    private val wc3MapsGamesWork = defaultUnitOfWork("wc3MapsGames")

    override suspend fun getGames(): List<Game> {
        val wc3StatsGames = emptyListOnException("wc3stats", WC3StatsHealthCheck) {
            wc3StatsGamesWork {
                wc3StatsClient.target("/gamelist")
                    .request(MediaType.APPLICATION_JSON)
                    .async()
                    .get(WC3StatsGames::class.java)
                    .await()
                    .body
            }
        }

        if (wc3StatsGames.isNotEmpty()) {
            return wc3StatsGames
        }

        return emptyListOnException("wc3maps", WC3MapsHealthCheck) {
            wc3MapsGamesWork {
                wc3MapsClient.target("/api/lobbies")
                    .request(MediaType.APPLICATION_JSON)
                    .async()
                    .get(WC3MapsGames::class.java)
                    .await()
                    .data
            }
        }
    }

    private suspend fun emptyListOnException(
        gamesSource: String,
        healthCheck: GameSourceHealthCheck,
        gamesProvider: suspend () -> List<Game>
    ): List<Game> = try {
        gamesProvider().also {
            if (it.isEmpty()) {
                healthCheck.noGames()
            } else {
                healthCheck.healthy()
            }
        }
    } catch (e: Exception) {
        logger.error("Exception when getting games from [ {} ]", gamesSource, e)
        healthCheck.exception(e)
        emptyList()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class WC3StatsGames(val body: List<WC3StatsGame>)

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class WC3MapsGames(val data: List<WC3MapsGame>)
}