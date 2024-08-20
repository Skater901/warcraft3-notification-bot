package au.com.skater901.wc3connect.core.domain

import au.com.skater901.wc3connect.api.core.domain.Game
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class GameImpl(
    override val id: Int,
    override val name: String,
    override val map: String,
    override val host: String,
    @JsonProperty("slots_taken")
    override val currentPlayers: Int,
    @JsonProperty("slots_total")
    override val maxPlayers: Int,
    @JsonDeserialize(using = WC3UptimeDeserializer::class)
    override val uptime: String
) : Game {
    internal class WC3UptimeDeserializer : JsonDeserializer<String>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): String = parser.valueAsString
            .split(":")
            .first()
    }
}