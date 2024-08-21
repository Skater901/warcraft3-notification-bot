package au.com.skater901.wc3connect.core.service

import au.com.skater901.wc3connect.api.core.domain.Game
import au.com.skater901.wc3connect.api.core.domain.exceptions.InvalidNotificationException
import au.com.skater901.wc3connect.api.core.service.GameNotifier
import au.com.skater901.wc3connect.core.dao.NotificationDAO
import au.com.skater901.wc3connect.utilities.collections.forEachAsync
import au.com.skater901.wc3connect.utilities.collections.mapAsync
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

@Singleton
internal class GameNotificationService @Inject constructor(
    private val notificationDAO: NotificationDAO,
    private val gameNotifiers: Map<String, @JvmSuppressWildcards GameNotifier>,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(GameNotificationService::class.java)
    }

    private val hostedGames = mutableMapOf<Int, Pair<Game, List<GameNotifier>>>()

    suspend fun notifyGames(currentlyHostedGames: List<Game>) {
        val newGames = currentlyHostedGames.filter { it.id !in hostedGames.keys }

        val gameIds = currentlyHostedGames.map { it.id }.toSet()

        val expiredGames = hostedGames.filter { (gameId, _) -> gameId !in gameIds }

        expiredGames.keys.forEach { hostedGames.remove(it) }

        coroutineScope {
            launch { updateExistingGames() }

            launch { closeExpiredGames(expiredGames.values.toList()) }

            postNewGames(newGames)
        }
            .forEach { hostedGames[it.first.id] = it }

        // Update the game info for hosted games
        currentlyHostedGames.forEach {
            val gameAndNotifiers = hostedGames[it.id]!!

            hostedGames[it.id] = it to gameAndNotifiers.second
        }
    }

    private suspend fun postNewGames(newGames: List<Game>): List<Pair<Game, List<GameNotifier>>> {
        val channelNotifications = notificationDAO.find()

        return newGames.mapAsync { game ->
            game to channelNotifications.filter { it.mapNameRegexPattern.containsMatchIn(game.map) }
                .mapAsync inner@{
                    val gameNotifier = gameNotifiers[it.type] ?: return@inner null
                    try {
                        gameNotifier.apply {
                            notifyNewGame(it.id, game)
                        }
                    } catch (e: InvalidNotificationException) {
                        logger.warn("Deleting invalid notification [ {}-{} ]", it.id, it.type)
                        notificationDAO.delete(it.id)
                        null
                    } catch (t: Throwable) {
                        LoggerFactory.getLogger(gameNotifier.javaClass)
                            .error(
                                "Exception when notifying new game for id [ {} ] in module [ {} ]. Game: {}",
                                it.id,
                                it.type,
                                game,
                                t
                            )
                        null
                    }
                }
                .filterNotNull()
        }
    }

    private suspend fun updateExistingGames() {
        hostedGames.values
            .forEachAsync { (game, notifiers) ->
                notifiers.forEachAsync {
                    try {
                        it.updateExistingGame(game)
                    } catch (t: Throwable) {
                        LoggerFactory.getLogger(it.javaClass)
                            .error("Exception when updating existing game [ {} ]", game, t)
                    }
                }
            }
    }

    private suspend fun closeExpiredGames(expiredGames: List<Pair<Game, List<GameNotifier>>>) {
        expiredGames.forEachAsync { (game, gameNotifiers) ->
            gameNotifiers.forEachAsync {
                try {
                    it.closeExpiredGame(game)
                } catch (t: Throwable) {
                    LoggerFactory.getLogger(it.javaClass)
                        .error("Exception when closing existing game [ {} ]", game, t)
                }
            }
        }
    }
}