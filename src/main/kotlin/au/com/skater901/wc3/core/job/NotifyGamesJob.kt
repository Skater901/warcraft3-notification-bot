package au.com.skater901.wc3.core.job

import au.com.skater901.wc3.core.gameProvider.GameProvider
import au.com.skater901.wc3.core.service.GameNotificationService
import au.com.skater901.wc3.utilities.collections.mapAsync
import au.com.skater901.wc3.utilities.coroutines.await
import com.fasterxml.jackson.databind.ObjectMapper
import io.dropwizard.lifecycle.Managed
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.ws.rs.core.MediaType
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

internal class NotifyGamesJob @Inject constructor(
    private val gameNotificationService: GameNotificationService,
    private val mapper: ObjectMapper,
    private val gameProviders: Set<@JvmSuppressWildcards GameProvider>,
    @param:Named("refreshInterval")
    private val refreshInterval: Long
) : Managed {
    companion object {
        private val logger = LoggerFactory.getLogger(NotifyGamesJob::class.java)
    }

    private lateinit var context: ExecutorCoroutineDispatcher
    private lateinit var job: Job

    override fun start() {
        // TODO remove
        context = newSingleThreadContext("notify-games-job")

        job = CoroutineScope(context).launch {
            while (true) {
                try {
                    // refresh
                    val games = gameProviders.mapAsync {
                        try {
                            it.webTarget()
                                .request(MediaType.APPLICATION_JSON)
                                .async()
                                .let { async -> it.getGames(async) }
                                .await()
                        } catch (t: Throwable) {
                            logger.error("Error when fetching games for {}", it::class.simpleName, t)
                            emptyList()
                        }
                    }
                        .flatten()

                    gameNotificationService.notifyGames(games)
                } catch (t: Throwable) {
                    logger.error("Error when fetching games list.", t)
                }

                delay(refreshInterval)
            }
        }
    }

    override fun stop() {
        job.cancel()
        context.close()
    }
}