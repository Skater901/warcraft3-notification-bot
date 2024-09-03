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

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WC3MapsGame(
    override val id: Int,
    override val name: String,
    @JsonProperty("path")
    override val map: String,
    override val host: String,
    @JsonProperty("slots_taken")
    override val currentPlayers: Int,
    @JsonProperty("slots_total")
    override val maxPlayers: Int,
    override val created: Instant,
    @JsonDeserialize(using = RegionDeserializer::class)
    override val region: Region
) : Game {
    override val gameSource: GameSource = GameSource.BattleNet

    internal class RegionDeserializer : JsonDeserializer<Region>() {
        override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): Region =
            when (parser.valueAsString) {
                "eu" -> Region.EU
                "usw" -> Region.US
                "kr" -> Region.Asia
                else -> Region.Unknown
            }
    }
}