package au.com.skater901.wc3connect.core.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonIgnoreProperties(ignoreUnknown = true)
public data class Game(
    val id: Int,
    val name: String,
    val map: String,
    val host: String,
    @JsonProperty("slots_taken")
    val currentPlayers: Int,
    @JsonProperty("slots_total")
    val maxPlayers: Int,
    @JsonDeserialize(using = WC3UptimeDeserializer::class)
    val uptime: String
) {
    internal class WC3UptimeDeserializer : JsonDeserializer<String>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): String = parser.valueAsString
            .split(":")
            .first()
    }
}