package au.com.skater901.wc3.core.domain

import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.api.core.domain.GameSource
import au.com.skater901.wc3.api.core.domain.Region
import au.com.skater901.wc3.core.convertors.RegionDeserializer
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class WC3MapsGame(
    override val name: String,
    @param:JsonProperty("path")
    override val map: String,
    override val host: String,
    @param:JsonProperty("slots_taken")
    override val currentPlayers: Int,
    @param:JsonProperty("slots_total")
    override val maxPlayers: Int,
    override val created: Instant,
    @param:JsonDeserialize(using = RegionDeserializer::class)
    override val region: Region
) : Game {
    override val id by lazy { name.hashCode() + map.hashCode() + host.hashCode() }

    override val gameSource = GameSource.BattleNet
}