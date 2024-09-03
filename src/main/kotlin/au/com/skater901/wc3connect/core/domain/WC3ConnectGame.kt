package au.com.skater901.wc3connect.core.domain

import au.com.skater901.wc3connect.api.core.domain.Game
import au.com.skater901.wc3connect.api.core.domain.GameSource
import au.com.skater901.wc3connect.api.core.domain.Region
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.Instant
import java.time.temporal.ChronoUnit

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WC3ConnectGame(
    override val id: Int,
    override val name: String,
    override val map: String,
    override val host: String,
    @JsonProperty("slots_taken")
    override val currentPlayers: Int,
    @JsonProperty("slots_total")
    override val maxPlayers: Int,
    @JsonProperty("uptime")
    @JsonDeserialize(using = UptimeDeserializer::class)
    override val created: Instant,
    @JsonProperty("location")
    @JsonDeserialize(using = RegionDeserializer::class)
    override val region: Region
) : Game {
    override val gameSource: GameSource = GameSource.WC3Connect

    internal class UptimeDeserializer : JsonDeserializer<Instant>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): Instant = parser.valueAsString
            .split(":")
            .let {
                Instant.now()
                    .minusSeconds(it[1].toLong())
                    .minus(it[0].toLong(), ChronoUnit.MINUTES)
            }
    }

    internal class RegionDeserializer : JsonDeserializer<Region>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): Region =
            when (parser.valueAsString) {
                "Montreal", "NewYork" -> Region.US
                "Amsterdam" -> Region.EU
                else -> Region.Unknown
            }
    }
}