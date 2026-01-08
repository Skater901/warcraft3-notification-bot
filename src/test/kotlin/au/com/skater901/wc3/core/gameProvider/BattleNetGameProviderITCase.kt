package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.core.domain.GameSource
import au.com.skater901.wc3.api.core.domain.Region
import au.com.skater901.wc3.utils.createClient
import au.com.skater901.wc3.utils.fixture
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.marcinziolo.kotlin.wiremock.*
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

@WireMockTest
class BattleNetGameProviderITCase {
    @Test
    fun `should get games from WC3Stats`(wireMockRuntime: WireMockRuntimeInfo) {
        wireMockRuntime.wireMock.get {
            url equalTo "/gamelist"

            headers contains "Accept" equalTo "application/json"
        } returns {
            header = HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON
            body = fixture("fixtures/wc3stats/games.json")
        }

        val games = createClient(wireMockRuntime).use {
            runBlocking { BattleNetGameProvider(it, it).getGames() }
        }

        assertThat(games).hasSize(4)
            .anyMatch {
                it.id == -1539066740 &&
                        it.name == "x hero" &&
                        it.map == "X_Hero_Reborn_1.2_ENG_fix~1.w3x" &&
                        it.host == "Nyxiz#2980" &&
                        it.currentPlayers == 8 &&
                        it.maxPlayers == 9 &&
                        it.region == Region.EU &&
                        it.gameSource == GameSource.BattleNet
            }
            .anyMatch {
                it.id == 258875721 &&
                        it.name == "greenTD" &&
                        it.map == "Green_HappyNewYear_Nightmare_FIXDESYNC~1.w3x" &&
                        it.host == "RoDac90#2504" &&
                        it.currentPlayers == 4 &&
                        it.maxPlayers == 9 &&
                        it.region == Region.US &&
                        it.gameSource == GameSource.BattleNet
            }
            .anyMatch {
                it.id == -1183358855 &&
                        it.name == "-phccezlg" &&
                        it.map == "Legion_TD_11.0k_TeamOZE.w3x" &&
                        it.host == "JosipBukal#2996" &&
                        it.currentPlayers == 1 &&
                        it.maxPlayers == 16 &&
                        it.region == Region.EU &&
                        it.gameSource == GameSource.BattleNet
            }
            .anyMatch {
                it.id == -898256637 &&
                        it.name == "-prccezlg" &&
                        it.map == "Legion_TD_11.0k_TeamOZE.w3x" &&
                        it.host == "JosipBukal#2996" &&
                        it.currentPlayers == 1 &&
                        it.maxPlayers == 16 &&
                        it.region == Region.Asia &&
                        it.gameSource == GameSource.BattleNet
            }

        wireMockRuntime.wireMock.verify {
            exactly = 0

            url equalTo "/api/lobbies"
        }
    }

    @Test
    fun `should fall back to WC3Maps if WC3Stats is empty`(wireMockRuntime: WireMockRuntimeInfo) {
        wireMockRuntime.wireMock.get {
            url equalTo "/gamelist"

            headers contains HttpHeaders.ACCEPT equalTo MediaType.APPLICATION_JSON
        } returns {
            body = "{\"body\":[]}"
        }
        wireMockRuntime.wireMock.get {
            url equalTo "/api/lobbies"

            headers contains HttpHeaders.ACCEPT equalTo MediaType.APPLICATION_JSON
        } returns {
            header = HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON
            body = fixture("fixtures/wc3maps/games.json")
        }

        val games = createClient(wireMockRuntime).use {
            runBlocking { BattleNetGameProvider(it, it).getGames() }
        }

        assertThat(games).hasSize(4)
            .anyMatch {
                it.id == -416127269 &&
                        it.name == "和" &&
                        it.map == "XCZYWCN9.5SZZWS.w3x" &&
                        it.host == "York#11394" &&
                        it.currentPlayers == 1 &&
                        it.maxPlayers == 10 &&
                        it.created == Instant.ofEpochSecond(1767671509) &&
                        it.region == Region.US &&
                        it.gameSource == GameSource.BattleNet
            }
            .anyMatch {
                it.id == 1143104989 &&
                        it.name == "악몽" &&
                        it.map == "ORDR_S2_2.201[R].w3x" &&
                        it.host == "와리가리디팬스#3967" &&
                        it.currentPlayers == 2 &&
                        it.maxPlayers == 4 &&
                        it.created == Instant.ofEpochSecond(1767671507) &&
                        it.region == Region.Asia &&
                        it.gameSource == GameSource.BattleNet
            }
            .anyMatch {
                it.id == 1193201636 &&
                        it.name == "imp3 up" &&
                        it.map == "Twilight Ascendant v3.06c.w3x" &&
                        it.host == "MrSunshine#1654" &&
                        it.currentPlayers == 9 &&
                        it.maxPlayers == 10 &&
                        it.created == Instant.ofEpochSecond(1767670451) &&
                        it.region == Region.US &&
                        it.gameSource == GameSource.BattleNet
            }
            .anyMatch {
                it.id == -765521156 &&
                        it.name == "Dota -APEMSOSP USW" &&
                        it.map == "DotA_v6_89N.w3x" &&
                        it.host == "JimmyJam#11761" &&
                        it.currentPlayers == 9 &&
                        it.maxPlayers == 11 &&
                        it.created == Instant.ofEpochSecond(1767670901) &&
                        it.region == Region.EU &&
                        it.gameSource == GameSource.BattleNet
            }
    }

    @Test
    fun `should fall back to WC3Maps if WC3Stats has an error`(wireMockRuntime: WireMockRuntimeInfo) {
        wireMockRuntime.wireMock.get {
            url equalTo "/gamelist"

            headers contains HttpHeaders.ACCEPT equalTo MediaType.APPLICATION_JSON
        } returns {
            statusCode = 500
        }
        wireMockRuntime.wireMock.get {
            url equalTo "/api/lobbies"

            headers contains HttpHeaders.ACCEPT equalTo MediaType.APPLICATION_JSON
        } returns {
            header = HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON
            body = fixture("fixtures/wc3maps/games.json")
        }

        val games = createClient(wireMockRuntime).use {
            runBlocking { BattleNetGameProvider(it, it).getGames() }
        }

        assertThat(games).hasSize(4)
            .anyMatch {
                it.id == -416127269 &&
                        it.name == "和" &&
                        it.map == "XCZYWCN9.5SZZWS.w3x" &&
                        it.host == "York#11394" &&
                        it.currentPlayers == 1 &&
                        it.maxPlayers == 10 &&
                        it.created == Instant.ofEpochSecond(1767671509) &&
                        it.region == Region.US &&
                        it.gameSource == GameSource.BattleNet
            }
            .anyMatch {
                it.id == 1143104989 &&
                        it.name == "악몽" &&
                        it.map == "ORDR_S2_2.201[R].w3x" &&
                        it.host == "와리가리디팬스#3967" &&
                        it.currentPlayers == 2 &&
                        it.maxPlayers == 4 &&
                        it.created == Instant.ofEpochSecond(1767671507) &&
                        it.region == Region.Asia &&
                        it.gameSource == GameSource.BattleNet
            }
            .anyMatch {
                it.id == 1193201636 &&
                        it.name == "imp3 up" &&
                        it.map == "Twilight Ascendant v3.06c.w3x" &&
                        it.host == "MrSunshine#1654" &&
                        it.currentPlayers == 9 &&
                        it.maxPlayers == 10 &&
                        it.created == Instant.ofEpochSecond(1767670451) &&
                        it.region == Region.US &&
                        it.gameSource == GameSource.BattleNet
            }
            .anyMatch {
                it.id == -765521156 &&
                        it.name == "Dota -APEMSOSP USW" &&
                        it.map == "DotA_v6_89N.w3x" &&
                        it.host == "JimmyJam#11761" &&
                        it.currentPlayers == 9 &&
                        it.maxPlayers == 11 &&
                        it.created == Instant.ofEpochSecond(1767670901) &&
                        it.region == Region.EU &&
                        it.gameSource == GameSource.BattleNet
            }
    }

    @Test
    fun `should return empty list if WC3Maps has an exception`(wireMockRuntime: WireMockRuntimeInfo) {
        wireMockRuntime.wireMock.get {
            url equalTo "/gamelist"

            headers contains HttpHeaders.ACCEPT equalTo MediaType.APPLICATION_JSON
        } returns {
            statusCode = 500
        }
        wireMockRuntime.wireMock.get {
            url equalTo "/api/lobbies"

            headers contains HttpHeaders.ACCEPT equalTo MediaType.APPLICATION_JSON
        } returns {
            statusCode = 500
        }

        val games = createClient(wireMockRuntime).use {
            runBlocking { BattleNetGameProvider(it, it).getGames() }
        }

        assertThat(games).isEmpty()
    }
}
