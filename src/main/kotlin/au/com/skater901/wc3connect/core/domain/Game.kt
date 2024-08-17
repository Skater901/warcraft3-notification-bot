package au.com.skater901.wc3connect.core.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonIgnoreProperties(ignoreUnknown = true)
public data class Game internal constructor(
    /**
     * The unique identifier of the Warcraft 3 hosted game.
     */
    val id: Int,

    /**
     * The name of the hosted game.
     */
    val name: String,

    /**
     * The Warcraft 3 map that this game is hosting.
     */
    val map: String,

    /**
     * The WC3Connect user who hosted this game. For games automatically hosted by WC3Connect, this value will be an empty String.
     */
    val host: String,

    /**
     * The number of players currently in the lobby.
     */
    @JsonProperty("slots_taken")
    val currentPlayers: Int,

    /**
     * The maximum number of players the hosted map supports.
     */
    @JsonProperty("slots_total")
    val maxPlayers: Int,

    /**
     * The number of minutes, as a String, since the game was hosted.
     */
    @JsonDeserialize(using = WC3UptimeDeserializer::class)
    val uptime: String
) {
    internal class WC3UptimeDeserializer : JsonDeserializer<String>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): String = parser.valueAsString
            .split(":")
            .first()
    }
}