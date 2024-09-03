package au.com.skater901.wc3connect.core.domain

import au.com.skater901.wc3connect.api.core.domain.Region
import com.fasterxml.jackson.core.JsonParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class WC3StatsGameTest {
    @Test
    fun `should deserialize US region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "usw"
        }

        val region = WC3StatsGame.RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.US)
    }

    @Test
    fun `should deserialize EU region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "eu"
        }

        val region = WC3StatsGame.RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.EU)
    }

    @Test
    fun `should deserialize Asia region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "kr"
        }

        val region = WC3StatsGame.RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.Asia)
    }

    @Test
    fun `should deserialize unknown region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "Arctic"
        }

        val region = WC3StatsGame.RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.Unknown)
    }
}