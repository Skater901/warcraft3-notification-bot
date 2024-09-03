package au.com.skater901.wc3connect.core.job

import au.com.skater901.wc3connect.core.gameProvider.GameProvider
import au.com.skater901.wc3connect.core.service.GameNotificationService
import au.com.skater901.wc3connect.utilities.collections.mapAsync
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

internal class NotifyGamesJob @Inject constructor(
    private val gameNotificationService: GameNotificationService,
    private val client: HttpClient,
    private val mapper: ObjectMapper,
    private val gameProviders: List<GameProvider>,
    @Named("refreshInterval")
    private val refreshInterval: Long
) : AutoCloseable {
    companion object {
        private val logger = LoggerFactory.getLogger(NotifyGamesJob::class.java)

        private val userAgent = "WC3Connect Notification Bot - Java-http-client/${System.getProperty("java.version")}"
    }

    private var dispatcher: CloseableCoroutineDispatcher? = null
    private var started = false

    fun start() {
        synchronized(this) {
            if (started) return@synchronized

            started = true

            dispatcher = newSingleThreadContext("game-notification")

            CoroutineScope(dispatcher!!).launch {
                while (started) {
                    try {
                        // refresh
                        val games = gameProviders.mapAsync {
                            val response = client.sendAsync(
                                HttpRequest.newBuilder(it.sourceURL)
                                    .header("Accept", "application/json")
                                    .header("User-Agent", userAgent)
                                    .build(),
                                BodyHandlers.ofInputStream()
                            )
                                .await()

                            if (response.statusCode() >= 400) {
                                throw RuntimeException(
                                    "HTTP error: ${response.statusCode()}, ${
                                        response.body().use { r -> r.use { r.reader().use { i -> i.readText() } } }
                                    }")
                            }

                            it.gamesProvider(mapper, response.body())
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
    }

    override fun close() {
        started = false

        dispatcher?.close()
        dispatcher = null
    }
}