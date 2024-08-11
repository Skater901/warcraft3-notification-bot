package au.com.skater901.w3cconnect.core.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Game(
    val id: Int,
    val name: String,
    val map: String,
    val host: String,
    @JsonProperty("slots_taken")
    val currentPlayers: Int,
    @JsonProperty("slots_total")
    val maxPlayers: Int
)