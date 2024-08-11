package au.com.skater901.w3cconnect.core.service

import au.com.skater901.w3cconnect.core.domain.Game

class GameNotificationService {
    suspend fun notifyGames(games: List<Game>) {
        println(games)
    }
}