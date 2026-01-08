package au.com.skater901.wc3.core.gameProvider

import au.com.skater901.wc3.api.core.domain.Game

internal interface GameProvider {
    suspend fun getGames(): List<Game>
}