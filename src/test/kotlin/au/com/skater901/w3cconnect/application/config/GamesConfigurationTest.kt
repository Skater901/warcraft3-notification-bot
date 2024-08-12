package au.com.skater901.w3cconnect.application.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.*

class GamesConfigurationTest {
    @Test
    fun `should raise error for invalid URL`() {
        val properties = Properties().apply {
            setProperty("client.gamesURL", "not a URL")
            setProperty("client.refreshInterval", "7")
        }

        assertThatThrownBy {
            GamesConfiguration.parse("client", properties)
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("[ client.gamesURL ] had invalid URL for fetching games: Illegal character in path at index 3: not a URL. Provided value was [ not a URL ]")
    }

    @Test
    fun `should raise error for refresh interval not being a number`() {
        val properties = Properties().apply {
            setProperty("client.gamesURL", "https://localhost")
            setProperty("client.refreshInterval", "not a number")
        }

        assertThatThrownBy {
            GamesConfiguration.parse("client", properties)
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("[ client.refreshInterval ] value provided was not a number, provided value was [ not a number ]")
    }

    @Test
    fun `should raise error for missing config properties`() {
        listOf(
            Properties().apply {
                setProperty("client.refreshInterval", "not a number")
            } to "client.gamesURL",
            Properties().apply {
                setProperty("client.gamesURL", "https://localhost")
            } to "client.refreshInterval"
        )
            .forEach { (properties, missingProperty) ->
                assertThatThrownBy {
                    GamesConfiguration.parse("client", properties)
                }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("No config property provided for [ $missingProperty ]")
            }
    }

    @Test
    fun `should parse config`() {
        val properties = Properties().apply {
            setProperty("client.gamesURL", "https://localhost")
            setProperty("client.refreshInterval", "10000")
        }

        val config = GamesConfiguration.parse("client", properties)
        assertThat(config.gamesURL.toString()).isEqualTo("https://localhost")
        assertThat(config.refreshInterval).isEqualTo(10_000)
    }
}