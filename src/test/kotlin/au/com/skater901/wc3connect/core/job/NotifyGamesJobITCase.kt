package au.com.skater901.wc3connect.core.job

import au.com.skater901.wc3connect.core.service.GameNotificationService
import au.com.skater901.wc3connect.utils.fixture
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.equalTo
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.returnsJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.net.URI
import java.net.http.HttpClient

@WireMockTest
class NotifyGamesJobITCase {
    companion object {
        private val mapper = ObjectMapper().registerKotlinModule()

        private val client = HttpClient.newHttpClient()

        @AfterAll
        @JvmStatic
        fun tearDown() {
            client.close()
        }
    }

    @Test
    fun `should fetch games list`(wireMock: WireMockRuntimeInfo) {
        wireMock.wireMock.get {
            url equalTo "/games"

            headers contains "Accept" equalTo "application/json"
        } returnsJson {
            body = fixture("fixtures/wc3connect/games.json")
        }

        val gameNotificationService = mock<GameNotificationService>()

        val job = NotifyGamesJob(
            gameNotificationService,
            client,
            URI("http://localhost:${wireMock.httpPort}/games"),
            mapper,
            1_000
        )

        runBlocking {
            job.use {
                it.start()

                delay(5_000)
            }
        }

        runBlocking {
            verify(gameNotificationService, atLeastOnce()).notifyGames(
                argThat {
                    size == 5 &&
                            any {
                                it.id == 4747 &&
                                        it.name == "[ENT] HELLHALT TD v80 #55" &&
                                        it.map == "HELLHALT v5.0.80" &&
                                        it.host == "" &&
                                        it.currentPlayers == 2 &&
                                        it.maxPlayers == 6
                            } &&
                            any {
                                it.id == 4176 &&
                                        it.name == "[ENT] HELLHALT TD v84 #25" &&
                                        it.map == "HELLHALT v5.0.84" &&
                                        it.host == "" &&
                                        it.currentPlayers == 0 &&
                                        it.maxPlayers == 6
                            } &&
                            any {
                                it.id == 4746 &&
                                        it.name == "[ENT] DotA apem us/ca #23" &&
                                        it.map == "DotA v6.83d fixed v5 by h3rmit" &&
                                        it.host == "" &&
                                        it.currentPlayers == 0 &&
                                        it.maxPlayers == 10
                            } &&
                            any {
                                it.id == 4745 &&
                                        it.name == "[ENT] Legion TD Mega 1v1 #65" &&
                                        it.map == "Legion TD Mega 3.43d6" &&
                                        it.host == "" &&
                                        it.currentPlayers == 0 &&
                                        it.maxPlayers == 2
                            } &&
                            any {
                                it.id == 3286 &&
                                        it.name == "[ENT] Castle Fight 1v1 #30" &&
                                        it.map == "p1l1s-CF-2040" &&
                                        it.host == "" &&
                                        it.currentPlayers == 0 &&
                                        it.maxPlayers == 2
                            }
                }
            )
        }
    }

    @Test
    fun `should handle exception when fetching games`(wireMock: WireMockRuntimeInfo) {
        wireMock.wireMock.get {
            url equalTo "/games"

            headers contains "Accept" equalTo "application/json"
        } returnsJson {
            statusCode = 500
            body = """{ "error": "blah" }"""
        }

        val gameNotificationService = mock<GameNotificationService>()

        val job = NotifyGamesJob(
            gameNotificationService,
            client,
            URI("http://localhost:${wireMock.httpPort}/games"),
            mapper,
            1_000
        )

        runBlocking {
            job.use {
                it.start()

                delay(5_000)
            }
        }

        verifyNoInteractions(gameNotificationService)
    }

    @Test
    fun `should handle being started twice, and being closed without being started`() {
        val gameNotificationService = mock<GameNotificationService>()

        val job = NotifyGamesJob(
            gameNotificationService,
            client,
            URI("http://localhost:8080/games"),
            mapper,
            1_000
        )

        job.start()
        job.start()

        job.close()
        job.close()
    }
}