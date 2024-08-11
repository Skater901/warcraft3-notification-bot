package au.com.skater901.w3cconnect.core.job

import au.com.skater901.w3cconnect.core.domain.Game
import au.com.skater901.w3cconnect.core.service.GameNotificationService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class NotifyGamesJob @Inject constructor(
    private val gameNotificationService: GameNotificationService,
    private val client: HttpClient,
    @Named("gamesURL")
    private val gamesUrl: URI,
    private val mapper: ObjectMapper,
    @Named("refreshInterval")
    private val refreshInterval: Long
) {
    companion object {
        private val logger = LoggerFactory.getLogger(NotifyGamesJob::class.java)
    }

    private var started = false

    fun start() {
        synchronized(this) {
            if (started) return@synchronized

            started = true

            CoroutineScope(newSingleThreadContext("game-notification")).launch {
                while (true) {
                    try {// refresh
                        client.sendAsync(
                            HttpRequest.newBuilder(gamesUrl)
                                .header("Accept", "application/json")
                                .build(),
                            BodyHandlers.ofInputStream()
                        )
                            .await()
                            .let { mapper.readValue<List<Game>>(it.body()) }
                            .let { gameNotificationService.notifyGames(it) }
                    } catch (t: Throwable) {
                        logger.error("Error when fetching games list.", t)
                    }

                    delay(refreshInterval)
                }
            }
        }
    }
}