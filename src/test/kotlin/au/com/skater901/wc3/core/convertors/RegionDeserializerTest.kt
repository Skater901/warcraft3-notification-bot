package au.com.skater901.wc3.core.convertors

import au.com.skater901.wc3.api.core.domain.Region
import com.fasterxml.jackson.core.JsonParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class RegionDeserializerTest {
    @Test
    fun `should deserialize US region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "usw"
        }

        val region = RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.US)
    }

    @Test
    fun `should deserialize EU region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "eu"
        }

        val region = RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.EU)
    }

    @Test
    fun `should deserialize Asia region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "kr"
        }

        val region = RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.Asia)
    }

    @Test
    fun `should deserialize unknown region`() {
        val parser = mock<JsonParser> {
            on { valueAsString } doReturn "Arctic"
        }

        val region = RegionDeserializer().deserialize(parser, mock())

        assertThat(region).isEqualTo(Region.Unknown)
    }
}