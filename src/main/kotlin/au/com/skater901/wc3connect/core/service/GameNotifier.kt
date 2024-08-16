package au.com.skater901.wc3connect.core.service

import au.com.skater901.wc3connect.core.domain.Game

public interface GameNotifier {
    public suspend fun notifyNewGame(notificationId: String, game: Game)

    public suspend fun updateExistingGame(game: Game)

    public suspend fun closeExpiredGame(game: Game)
}