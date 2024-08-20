package au.com.skater901.wc3connect.api.core.service

import au.com.skater901.wc3connect.api.core.domain.Game

/**
 * The interface to implement to handle notification of WC3Connect games being hosted and unhosted.
 */
public interface GameNotifier {
    /**
     * Send a notification when a new game is hosted. You MUST store the ID of the created notification so that it can
     * be amended when the game state changes. (Unless you would prefer to simply create new notifications for every
     * game state change.)
     *
     * @param notificationId The unique identifier of the recipient of this notification. This value will vary depending
     * on your implementation. It is up to you if you wish to verify that the [notificationId] is a valid ID for your
     * implementation.
     *
     * @param game The details about the Warcraft 3 game hosted on WC3Connect.
     *
     * @throws [au.com.skater901.wc3connect.api.core.domain.exceptions.InvalidNotificationException]
     */
    public suspend fun notifyNewGame(notificationId: String, game: Game)

    /**
     * Update the status of a hosted game. Please note, this function will be called even if the game state hasn't changed.
     * It's up to you if you want to update notifications every time this function is called, or if you would prefer to
     * implement a mechanism to detect when game state has changed, and only update notifications when game state changes.
     *
     * @param game The details about the Warcraft 3 game hosted on WC3Connect.
     */
    public suspend fun updateExistingGame(game: Game)

    /**
     * Indicate that the given game no longer exists. This could be because it was unhosted, or because it started.
     *
     * @param game The details about the Warcraft 3 game that is no longer hosted.
     */
    public suspend fun closeExpiredGame(game: Game)
}