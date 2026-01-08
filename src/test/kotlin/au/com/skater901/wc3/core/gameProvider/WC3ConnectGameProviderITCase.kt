package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.core.domain.GameSource
import au.com.skater901.wc3.api.core.domain.Region
import au.com.skater901.wc3.utils.createClient
import au.com.skater901.wc3.utils.fixture
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.equalTo
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.returns
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WireMockTest
class WC3ConnectGameProviderITCase {
    @Test
    fun `should get games`(wireMockInfo: WireMockRuntimeInfo) {
        wireMockInfo.wireMock.get {
            url equalTo "/allgames"

            headers contains "Accept" equalTo "application/json"
        } returns {
            header = HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON
            body = fixture("fixtures/wc3connect/games.json")
        }

        val games = createClient(wireMockInfo).use {
            runBlocking { WC3ConnectGameProvider(it).getGames() }
        }

        assertThat(games).hasSize(5)
            .anyMatch {
                it.id == 4747 &&
                        it.name == "[ENT] HELLHALT TD v80 #55" &&
                        it.map == "HELLHALT v5.0.80" &&
                        it.host == "" &&
                        it.currentPlayers == 2 &&
                        it.maxPlayers == 6 &&
                        it.region == Region.US &&
                        it.gameSource == GameSource.WC3Connect
            }
            .anyMatch {
                it.id == 4176 &&
                        it.name == "[ENT] HELLHALT TD v84 #25" &&
                        it.map == "HELLHALT v5.0.84" &&
                        it.host == "" &&
                        it.currentPlayers == 0 &&
                        it.maxPlayers == 6 &&
                        it.region == Region.US &&
                        it.gameSource == GameSource.WC3Connect
            }
            .anyMatch {
                it.id == 4746 &&
                        it.name == "[ENT] DotA apem us/ca #23" &&
                        it.map == "DotA v6.83d fixed v5 by h3rmit" &&
                        it.host == "" &&
                        it.currentPlayers == 0 &&
                        it.maxPlayers == 10 &&
                        it.region == Region.US &&
                        it.gameSource == GameSource.WC3Connect
            }
            .anyMatch {
                it.id == 4745 &&
                        it.name == "[ENT] Legion TD Mega 1v1 #65" &&
                        it.map == "Legion TD Mega 3.43d6" &&
                        it.host == "" &&
                        it.currentPlayers == 0 &&
                        it.maxPlayers == 2 &&
                        it.region == Region.US &&
                        it.gameSource == GameSource.WC3Connect
            }
            .anyMatch {
                it.id == 3286 &&
                        it.name == "[ENT] Castle Fight 1v1 #30" &&
                        it.map == "p1l1s-CF-2040" &&
                        it.host == "test host" &&
                        it.currentPlayers == 0 &&
                        it.maxPlayers == 2 &&
                        it.region == Region.EU &&
                        it.gameSource == GameSource.WC3Connect
            }
    }
}