package au.com.skater901.wc3connect.core.domain

import au.com.skater901.wc3connect.api.core.domain.Region
import com.fasterxml.jackson.core.JsonParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub

class WC3ConnectGameTest {
    @Test
    fun `should deserialize US region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "Montreal"
        }

        var region = WC3ConnectGame.RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.US)

        reset(parser)
        parser.stub {
            on { valueAsString } doReturn "NewYork"
        }

        region = WC3ConnectGame.RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.US)
    }

    @Test
    fun `should deserialize EU region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "Amsterdam"
        }

        val region = WC3ConnectGame.RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.EU)
    }

    @Test
    fun `should deserialize unknown region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "Antarctica"
        }

        val region = WC3ConnectGame.RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.Unknown)
    }
}