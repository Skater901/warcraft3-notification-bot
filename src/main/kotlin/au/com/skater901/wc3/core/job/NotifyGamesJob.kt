package au.com.skater901.wc3.core.job

import au.com.skater901.wc3.core.gameProvider.GameProvider
import au.com.skater901.wc3.core.service.GameNotificationService
import au.com.skater901.wc3.utilities.collections.mapAsync
import au.com.skater901.wc3.utilities.metricsWork
import io.dropwizard.lifecycle.Managed
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService

internal class NotifyGamesJob @Inject constructor(
    @Named("notify-games-job-thread-pool")
    notifyGamesJobExecutor: ExecutorService,
    private val gameNotificationService: GameNotificationService,
    private val gameProviders: Set<@JvmSuppressWildcards GameProvider>,
    @param:Named("refreshInterval")
    private val refreshInterval: Long
) : Managed {
    companion object {
        private val logger = LoggerFactory.getLogger(NotifyGamesJob::class.java)
    }

    private val context = notifyGamesJobExecutor.asCoroutineDispatcher()
    private lateinit var job: Job

    private val notifyGamesWork = metricsWork("notifyGames")

    override fun start() {
        job = CoroutineScope(context).launch {
            while (true) {
                notifyGamesWork {
                    try {
                        // refresh
                        val games = gameProviders.mapAsync {
                            try {
                                it.getGames()
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
                }

                delay(refreshInterval)
            }
        }
    }

    override fun stop() {
        job.cancel()
    }
}