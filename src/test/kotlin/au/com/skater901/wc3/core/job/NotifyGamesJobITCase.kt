package au.com.skater901.wc3.core.job

import au.com.skater901.wc3.api.core.domain.GameSource
import au.com.skater901.wc3.api.core.domain.Region
import au.com.skater901.wc3.application.config.WC3ConnectConfig
import au.com.skater901.wc3.application.config.WC3StatsConfig
import au.com.skater901.wc3.core.gameProvider.WC3ConnectGameProvider
import au.com.skater901.wc3.core.gameProvider.WC3StatsGameProvider
import au.com.skater901.wc3.core.service.GameNotificationService
import au.com.skater901.wc3.utils.fixture
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.URI
import java.net.http.HttpClient

@WireMockTest
class NotifyGamesJobITCase {
    companion object {
        private val mapper = ObjectMapper().registerKotlinModule()
            .registerModule(JavaTimeModule())

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
            url equalTo "/allgames"

            headers contains "Accept" equalTo "application/json"
        } returnsJson {
            body = fixture("fixtures/wc3connect/games.json")
        }
        wireMock.wireMock.get {
            url equalTo "/api/lobbies"

            headers contains "Accept" equalTo "application/json"
        } returnsJson {
            body = fixture("fixtures/wc3stats/games.json")
        }

        val gameNotificationService = mock<GameNotificationService>()

        val job = NotifyGamesJob(
            gameNotificationService,
            client,
            mapper,
            setOf(
                WC3ConnectGameProvider(WC3ConnectConfig(URI("http://localhost:${wireMock.httpPort}/allgames"))),
                WC3StatsGameProvider(WC3StatsConfig(URI("http://localhost:${wireMock.httpPort}/api/lobbies")))
            ),
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
                    size == 9 &&
                            any {
                                it.id == 4747 &&
                                        it.name == "[ENT] HELLHALT TD v80 #55" &&
                                        it.map == "HELLHALT v5.0.80" &&
                                        it.host == "" &&
                                        it.currentPlayers == 2 &&
                                        it.maxPlayers == 6 &&
                                        it.region == Region.US &&
                                        it.gameSource == GameSource.WC3Connect
                            } &&
                            any {
                                it.id == 4176 &&
                                        it.name == "[ENT] HELLHALT TD v84 #25" &&
                                        it.map == "HELLHALT v5.0.84" &&
                                        it.host == "" &&
                                        it.currentPlayers == 0 &&
                                        it.maxPlayers == 6 &&
                                        it.region == Region.US &&
                                        it.gameSource == GameSource.WC3Connect
                            } &&
                            any {
                                it.id == 4746 &&
                                        it.name == "[ENT] DotA apem us/ca #23" &&
                                        it.map == "DotA v6.83d fixed v5 by h3rmit" &&
                                        it.host == "" &&
                                        it.currentPlayers == 0 &&
                                        it.maxPlayers == 10 &&
                                        it.region == Region.US &&
                                        it.gameSource == GameSource.WC3Connect
                            } &&
                            any {
                                it.id == 4745 &&
                                        it.name == "[ENT] Legion TD Mega 1v1 #65" &&
                                        it.map == "Legion TD Mega 3.43d6" &&
                                        it.host == "" &&
                                        it.currentPlayers == 0 &&
                                        it.maxPlayers == 2 &&
                                        it.region == Region.US &&
                                        it.gameSource == GameSource.WC3Connect
                            } &&
                            any {
                                it.id == 3286 &&
                                        it.name == "[ENT] Castle Fight 1v1 #30" &&
                                        it.map == "p1l1s-CF-2040" &&
                                        it.host == "" &&
                                        it.currentPlayers == 0 &&
                                        it.maxPlayers == 2 &&
                                        it.region == Region.US &&
                                        it.gameSource == GameSource.WC3Connect
                            } &&
                            any {
                                it.id == 19342 &&
                                        it.name == "x hero" &&
                                        it.map == "X_Hero_Reborn_1.2_ENG_fix~1.w3x" &&
                                        it.host == "Nyxiz#2980" &&
                                        it.currentPlayers == 8 &&
                                        it.maxPlayers == 9 &&
                                        it.region == Region.EU &&
                                        it.gameSource == GameSource.BattleNet
                            } &&
                            any {
                                it.id == 21541 &&
                                        it.name == "greenTD" &&
                                        it.map == "Green_HappyNewYear_Nightmare_FIXDESYNC~1.w3x" &&
                                        it.host == "RoDac90#2504" &&
                                        it.currentPlayers == 4 &&
                                        it.maxPlayers == 9 &&
                                        it.region == Region.EU &&
                                        it.gameSource == GameSource.BattleNet
                            } &&
                            any {
                                it.id == 56252 &&
                                        it.name == "-phccezlg" &&
                                        it.map == "Legion_TD_11.0k_TeamOZE.w3x" &&
                                        it.host == "JosipBukal#2996" &&
                                        it.currentPlayers == 1 &&
                                        it.maxPlayers == 16 &&
                                        it.region == Region.EU &&
                                        it.gameSource == GameSource.BattleNet
                            } &&
                            any {
                                it.id == 56258 &&
                                        it.name == "-prccezlg" &&
                                        it.map == "Legion_TD_11.0k_TeamOZE.w3x" &&
                                        it.host == "JosipBukal#2996" &&
                                        it.currentPlayers == 1 &&
                                        it.maxPlayers == 16 &&
                                        it.region == Region.EU &&
                                        it.gameSource == GameSource.BattleNet
                            }
                }
            )
        }
    }

    @Test
    fun `should handle exception when fetching games`(wireMock: WireMockRuntimeInfo) {
        wireMock.wireMock.get {
            url equalTo "/allgames"

            headers contains "Accept" equalTo "application/json"
        } returnsJson {
            statusCode = 500
            body = """{ "error": "blah" }"""
        }
        wireMock.wireMock.get {
            url equalTo "/api/lobbies"

            headers contains "Accept" equalTo "application/json"
        } returnsJson {
            body = fixture("fixtures/wc3stats/games.json")
        }

        val gameNotificationService = mock<GameNotificationService>()

        val job = NotifyGamesJob(
            gameNotificationService,
            client,
            mapper,
            setOf(
                WC3ConnectGameProvider(WC3ConnectConfig(URI("http://localhost:${wireMock.httpPort}/allgames"))),
                WC3StatsGameProvider(WC3StatsConfig(URI("http://localhost:${wireMock.httpPort}/api/lobbies")))
            ),
            1_000
        )

        runBlocking {
            job.use {
                it.start()

                delay(5_000)
            }
        }

        runBlocking {
            verify(gameNotificationService, atLeastOnce()).notifyGames(argThat { size == 4 })
        }
    }

    @Test
    fun `should handle being started twice, and being closed without being started`() {
        val gameNotificationService = mock<GameNotificationService>()

        val job = NotifyGamesJob(
            gameNotificationService,
            client,
            mapper,
            emptySet(),
            1_000
        )

        job.start()
        job.start()

        job.close()
        job.close()
    }
}