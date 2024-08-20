package au.com.skater901.wc3connect.core.job

import au.com.skater901.wc3connect.core.domain.GameImpl
import au.com.skater901.wc3connect.core.service.GameNotificationService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

internal class NotifyGamesJob @Inject constructor(
    private val gameNotificationService: GameNotificationService,
    private val client: HttpClient,
    @Named("gamesURL")
    private val gamesUrl: URI,
    private val mapper: ObjectMapper,
    @Named("refreshInterval")
    private val refreshInterval: Long
) : AutoCloseable {
    companion object {
        private val logger = LoggerFactory.getLogger(NotifyGamesJob::class.java)
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
                        client.sendAsync(
                            HttpRequest.newBuilder(gamesUrl)
                                .header("Accept", "application/json")
                                .build(),
                            BodyHandlers.ofInputStream()
                        )
                            .await()
                            .let {
                                if (it.statusCode() >= 400) {
                                    throw RuntimeException(
                                        "HTTP error: ${it.statusCode()}, ${
                                            it.body().use { r -> r.use { r.reader().use { i -> i.readText() } } }
                                        }")
                                }

                                mapper.readValue<List<GameImpl>>(it.body())
                            }
                            .let { gameNotificationService.notifyGames(it) }
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