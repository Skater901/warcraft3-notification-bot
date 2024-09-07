package au.com.skater901.wc3.core.job

import au.com.skater901.wc3.core.gameProvider.GameProvider
import au.com.skater901.wc3.core.service.GameNotificationService
import au.com.skater901.wc3.utilities.collections.mapAsync
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

internal class NotifyGamesJob @Inject constructor(
    private val gameNotificationService: GameNotificationService,
    private val client: HttpClient,
    private val mapper: ObjectMapper,
    private val gameProviders: Set<@JvmSuppressWildcards GameProvider>,
    @Named("refreshInterval")
    private val refreshInterval: Long
) {
    companion object {
        private val logger = LoggerFactory.getLogger(NotifyGamesJob::class.java)

        private val userAgent =
            "WC3 Notification Bot ${System.getProperty("appVersion")} - Java-http-client/${System.getProperty("java.version")}"

        private val requestTimeout = Duration.ofSeconds(10)
    }

    suspend fun start() {
        while (true) {
            try {
                // refresh
                val games = gameProviders.mapAsync {
                    try {
                        val response = client.sendAsync(
                            HttpRequest.newBuilder(it.sourceURL)
                                .header("Accept", "application/json")
                                .header("User-Agent", userAgent)
                                .timeout(requestTimeout)
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
                    } catch (t: Throwable) {
                        logger.error("Error when fetching games for {}", it.name, t)
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