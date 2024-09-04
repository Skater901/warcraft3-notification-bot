package au.com.skater901.wc3connect.discord.utils

import au.com.skater901.wc3connect.api.core.domain.Game
import au.com.skater901.wc3connect.api.core.domain.GameSource
import au.com.skater901.wc3connect.api.core.domain.Region
import java.time.Instant

class GameBuilder : Game {
    override var id = 1
    override var name = "My cool game"
    override var map = "Best_Map.w3x"
    override var host = "best_war3_player"
    override var currentPlayers = 1
    override var maxPlayers = 8
    override var created: Instant = Instant.now()
    override var region = Region.US
    override var gameSource = GameSource.BattleNet
}

fun game(builder: GameBuilder.() -> Unit = {}): Game = GameBuilder().apply(builder)