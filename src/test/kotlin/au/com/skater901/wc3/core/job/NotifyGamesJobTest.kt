package au.com.skater901.wc3.core.job

import au.com.skater901.wc3.api.core.domain.GameSource
import au.com.skater901.wc3.api.core.domain.Region
import au.com.skater901.wc3.core.domain.WC3ConnectGame
import au.com.skater901.wc3.core.domain.WC3StatsGame
import au.com.skater901.wc3.core.service.GameNotificationService
import jakarta.ws.rs.InternalServerErrorException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.util.concurrent.Executors

class NotifyGamesJobTest {
    @Test
    fun `should fetch games list`() {
        val gameNotificationService = mock<GameNotificationService>()

        Executors.newSingleThreadExecutor()
            .use {
                val job = NotifyGamesJob(
                    it,
                    gameNotificationService,
                    setOf(
                        mock {
                            on { getGames() } doReturn listOf(
                                WC3ConnectGame(
                                    4747,
                                    "[ENT] HELLHALT TD v80 #55",
                                    "HELLHALT v5.0.80",
                                    "",
                                    2,
                                    6,
                                    Instant.now(),
                                    Region.US
                                ),
                                WC3ConnectGame(
                                    4176,
                                    "[ENT] HELLHALT TD v84 #25",
                                    "HELLHALT v5.0.84",
                                    "",
                                    0,
                                    6,
                                    Instant.now(),
                                    Region.US
                                ),
                                WC3ConnectGame(
                                    4746,
                                    "[ENT] DotA apem us/ca #23",
                                    "DotA v6.83d fixed v5 by h3rmit",
                                    "",
                                    0,
                                    10,
                                    Instant.now(),
                                    Region.US
                                ),
                                WC3ConnectGame(
                                    4745,
                                    "[ENT] Legion TD Mega 1v1 #65",
                                    "Legion TD Mega 3.43d6",
                                    "",
                                    0,
                                    2,
                                    Instant.now(),
                                    Region.US
                                ),
                                WC3ConnectGame(
                                    3286,
                                    "[ENT] Castle Fight 1v1 #30",
                                    "p1l1s-CF-2040",
                                    "test host",
                                    0,
                                    2,
                                    Instant.now(),
                                    Region.EU
                                )
                            )
                        },
                        mock {
                            on { getGames() } doReturn listOf(
                                WC3StatsGame(
                                    "x hero",
                                    "X_Hero_Reborn_1.2_ENG_fix~1.w3x",
                                    "Nyxiz#2980",
                                    8,
                                    9,
                                    Instant.ofEpochSecond(1725173795),
                                    Region.EU
                                ),
                                WC3StatsGame(
                                    "greenTD",
                                    "Green_HappyNewYear_Nightmare_FIXDESYNC~1.w3x",
                                    "RoDac90#2504",
                                    4,
                                    9,
                                    Instant.ofEpochSecond(1725180519),
                                    Region.EU
                                ),
                                WC3StatsGame(
                                    "-phccezlg",
                                    "Legion_TD_11.0k_TeamOZE.w3x",
                                    "JosipBukal#2996",
                                    1,
                                    16,
                                    Instant.ofEpochSecond(1725278870),
                                    Region.EU
                                ),
                                WC3StatsGame(
                                    "-prccezlg",
                                    "Legion_TD_11.0k_TeamOZE.w3x",
                                    "JosipBukal#2996",
                                    1,
                                    16,
                                    Instant.ofEpochSecond(1725278895),
                                    Region.EU
                                )
                            )
                        }
                    ),
                    1_000
                )

                runBlocking {
                    job.start()

                    delay(5_000)

                    job.stop()
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
                                        it.host == "test host" &&
                                        it.currentPlayers == 0 &&
                                        it.maxPlayers == 2 &&
                                        it.region == Region.EU &&
                                        it.gameSource == GameSource.WC3Connect
                            } &&
                            any {
                                it.id == -1539066740 &&
                                        it.name == "x hero" &&
                                        it.map == "X_Hero_Reborn_1.2_ENG_fix~1.w3x" &&
                                        it.host == "Nyxiz#2980" &&
                                        it.currentPlayers == 8 &&
                                        it.maxPlayers == 9 &&
                                        it.region == Region.EU &&
                                        it.gameSource == GameSource.BattleNet
                            } &&
                            any {
                                it.id == 258875721 &&
                                        it.name == "greenTD" &&
                                        it.map == "Green_HappyNewYear_Nightmare_FIXDESYNC~1.w3x" &&
                                        it.host == "RoDac90#2504" &&
                                        it.currentPlayers == 4 &&
                                        it.maxPlayers == 9 &&
                                        it.region == Region.EU &&
                                        it.gameSource == GameSource.BattleNet
                            } &&
                            any {
                                it.id == -1183358855 &&
                                        it.name == "-phccezlg" &&
                                        it.map == "Legion_TD_11.0k_TeamOZE.w3x" &&
                                        it.host == "JosipBukal#2996" &&
                                        it.currentPlayers == 1 &&
                                        it.maxPlayers == 16 &&
                                        it.region == Region.EU &&
                                        it.gameSource == GameSource.BattleNet
                            } &&
                            any {
                                it.id == -898256637 &&
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
    fun `should handle exception when fetching games`() {
        val gameNotificationService = mock<GameNotificationService>()

        Executors.newSingleThreadExecutor()
            .use {
                val job = NotifyGamesJob(
                    it,
                    gameNotificationService,
                    setOf(
                        mock {
                            on { getGames() } doThrow InternalServerErrorException("blah")
                        },
                        mock {
                            on { getGames() } doReturn listOf(
                                WC3StatsGame(
                                    "x hero",
                                    "X_Hero_Reborn_1.2_ENG_fix~1.w3x",
                                    "Nyxiz#2980",
                                    8,
                                    9,
                                    Instant.ofEpochSecond(1725173795),
                                    Region.EU
                                ),
                                WC3StatsGame(
                                    "greenTD",
                                    "Green_HappyNewYear_Nightmare_FIXDESYNC~1.w3x",
                                    "RoDac90#2504",
                                    4,
                                    9,
                                    Instant.ofEpochSecond(1725180519),
                                    Region.EU
                                ),
                                WC3StatsGame(
                                    "-phccezlg",
                                    "Legion_TD_11.0k_TeamOZE.w3x",
                                    "JosipBukal#2996",
                                    1,
                                    16,
                                    Instant.ofEpochSecond(1725278870),
                                    Region.EU
                                ),
                                WC3StatsGame(
                                    "-prccezlg",
                                    "Legion_TD_11.0k_TeamOZE.w3x",
                                    "JosipBukal#2996",
                                    1,
                                    16,
                                    Instant.ofEpochSecond(1725278895),
                                    Region.EU
                                )
                            )
                        }
                    ),
                    1_000
                )

                runBlocking {
                    job.start()

                    delay(5_000)

                    job.stop()
                }
            }

        runBlocking {
            verify(gameNotificationService, atLeastOnce()).notifyGames(argThat { size == 4 })
        }
    }
}