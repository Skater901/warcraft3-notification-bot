package au.com.skater901.wc3.core.service

import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.api.core.domain.exceptions.InvalidNotificationException
import au.com.skater901.wc3.api.core.service.GameNotifier
import au.com.skater901.wc3.core.dao.NotificationDAO
import au.com.skater901.wc3.core.domain.WC3GameNotification
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class GameNotificationServiceTest {
    @Test
    fun `should announce new game`() {
        val module = "mymodule"
        val notificationId = "myid"
        val mapRegex = "mymap"

        val notificationDAO = mock<NotificationDAO> {
            onBlocking { find() } doReturn listOf(WC3GameNotification(notificationId, module, Regex(mapRegex)))
        }

        val myModuleNotifier = mock<GameNotifier>()

        val notifiers = mapOf(module to myModuleNotifier)

        val newGame = mock<Game> {
            on { id } doReturn 1234
            on { map } doReturn mapRegex
        }

        val gameNotificationService = GameNotificationService(notificationDAO, notifiers)

        runBlocking {
            gameNotificationService.notifyGames(listOf(newGame))
        }

        verifyBlocking(myModuleNotifier) {
            notifyNewGame(notificationId, newGame)
        }
    }

    @Test
    fun `should announce existing game`() {
        val module = "mymodule"
        val notificationId = "myid"
        val mapRegex = "mymap"

        val notificationDAO = mock<NotificationDAO> {
            onBlocking { find() } doReturn listOf(WC3GameNotification(notificationId, module, Regex(mapRegex)))
        }

        val myModuleNotifier = mock<GameNotifier>()

        val notifiers = mapOf(module to myModuleNotifier)

        val newGame = mock<Game> {
            on { id } doReturn 1234
            on { map } doReturn mapRegex
        }

        val gameNotificationService = GameNotificationService(notificationDAO, notifiers)

        runBlocking {
            repeat(2) { gameNotificationService.notifyGames(listOf(newGame)) }
        }

        verifyBlocking(myModuleNotifier) {
            updateExistingGame(newGame)
        }
    }

    @Test
    fun `should announce expired game`() {
        val module = "mymodule"
        val notificationId = "myid"
        val mapRegex = "mymap"

        val notificationDAO = mock<NotificationDAO> {
            onBlocking { find() } doReturn listOf(WC3GameNotification(notificationId, module, Regex(mapRegex)))
        }

        val myModuleNotifier = mock<GameNotifier>()

        val notifiers = mapOf(module to myModuleNotifier)

        val newGame = mock<Game> {
            on { id } doReturn 1234
            on { map } doReturn mapRegex
        }

        val gameNotificationService = GameNotificationService(notificationDAO, notifiers)

        runBlocking {
            gameNotificationService.notifyGames(listOf(newGame))
            gameNotificationService.notifyGames(emptyList())
        }

        verifyBlocking(myModuleNotifier) {
            closeExpiredGame(newGame)
        }
    }

    @Test
    fun `should only announce new game to notifiers where map name matches regex`() {
        val module = "mymodule"
        val notificationId = "myid"
        val mapRegex = "mymap"

        val notificationDAO = mock<NotificationDAO> {
            onBlocking { find() } doReturn listOf(WC3GameNotification(notificationId, module, Regex(mapRegex)))
        }

        val myModuleNotifier = mock<GameNotifier>()

        val notifiers = mapOf(module to myModuleNotifier)

        val newGame = mock<Game> {
            on { id } doReturn 1234
            on { map } doReturn mapRegex
        }
        val newGame2 = mock<Game> {
            on { id } doReturn 1111
            on { map } doReturn "a different map"
        }

        val gameNotificationService = GameNotificationService(notificationDAO, notifiers)

        runBlocking {
            gameNotificationService.notifyGames(listOf(newGame, newGame2))
        }

        verifyBlocking(myModuleNotifier) {
            notifyNewGame(notificationId, newGame)
        }
        verifyBlocking(myModuleNotifier, never()) {
            notifyNewGame(notificationId, newGame2)
        }
    }

    @Test
    fun `should only announce new game to notifiers for module of notification`() {
        val module = "mymodule"
        val notificationId = "myid"
        val mapRegex = "mymap"

        val notificationDAO = mock<NotificationDAO> {
            onBlocking { find() } doReturn listOf(WC3GameNotification(notificationId, module, Regex(mapRegex)))
        }

        val myModuleNotifier = mock<GameNotifier>()
        val myOtherNotifier = mock<GameNotifier>()

        val notifiers = mapOf(module to myModuleNotifier, "other" to myOtherNotifier)

        val newGame = mock<Game> {
            on { id } doReturn 1234
            on { map } doReturn mapRegex
        }

        val gameNotificationService = GameNotificationService(notificationDAO, notifiers)

        runBlocking {
            gameNotificationService.notifyGames(listOf(newGame))
        }

        verifyBlocking(myModuleNotifier) {
            notifyNewGame(notificationId, newGame)
        }
        verifyNoInteractions(myOtherNotifier)
    }

    @Test
    fun `should delete notification if InvalidNotificationException thrown when notifying new game`() {
        val module = "mymodule"
        val notificationId = "myid"
        val mapRegex = "mymap"

        val notificationDAO = mock<NotificationDAO> {
            onBlocking { find() } doReturn listOf(WC3GameNotification(notificationId, module, Regex(mapRegex)))
        }

        val newGame = mock<Game> {
            on { id } doReturn 1234
            on { map } doReturn mapRegex
        }

        val myModuleNotifier = mock<GameNotifier> {
            onBlocking { notifyNewGame(notificationId, newGame) } doThrow InvalidNotificationException()
        }

        val notifiers = mapOf(module to myModuleNotifier)

        val gameNotificationService = GameNotificationService(notificationDAO, notifiers)

        runBlocking {
            gameNotificationService.notifyGames(listOf(newGame))
        }

        verifyBlocking(myModuleNotifier) {
            notifyNewGame(notificationId, newGame)
        }
        verifyBlocking(notificationDAO) {
            delete(notificationId)
        }
    }

    @Test
    fun `should handle exception thrown when notifying new game`() {
        val module = "mymodule"
        val notificationId = "myid"
        val mapRegex = "mymap"

        val notificationDAO = mock<NotificationDAO> {
            onBlocking { find() } doReturn listOf(WC3GameNotification(notificationId, module, Regex(mapRegex)))
        }

        val newGame = mock<Game> {
            on { id } doReturn 1234
            on { map } doReturn mapRegex
        }

        val myModuleNotifier = mock<GameNotifier> {
            onBlocking { notifyNewGame(notificationId, newGame) } doThrow RuntimeException("Kaboom!")
        }

        val notifiers = mapOf(module to myModuleNotifier)

        val gameNotificationService = GameNotificationService(notificationDAO, notifiers)

        runBlocking {
            gameNotificationService.notifyGames(listOf(newGame))
        }

        verifyBlocking(myModuleNotifier) {
            notifyNewGame(notificationId, newGame)
        }
    }

    @Test
    fun `should handle exception thrown when updating existing game`() {
        val module = "mymodule"
        val notificationId = "myid"
        val mapRegex = "mymap"

        val notificationDAO = mock<NotificationDAO> {
            onBlocking { find() } doReturn listOf(WC3GameNotification(notificationId, module, Regex(mapRegex)))
        }

        val newGame = mock<Game> {
            on { id } doReturn 1234
            on { map } doReturn mapRegex
        }

        val myModuleNotifier = mock<GameNotifier> {
            onBlocking { updateExistingGame(newGame) } doThrow RuntimeException("Kaboom!")
        }

        val notifiers = mapOf(module to myModuleNotifier)

        val gameNotificationService = GameNotificationService(notificationDAO, notifiers)

        runBlocking {
            repeat(2) { gameNotificationService.notifyGames(listOf(newGame)) }
        }

        verify(myModuleNotifier) {
            1 * { runBlocking { notifyNewGame(notificationId, newGame) } }
            1 * { runBlocking { updateExistingGame(newGame) } }
        }
    }

    @Test
    fun `should handle exception thrown when closing expired game`() {
        val module = "mymodule"
        val notificationId = "myid"
        val mapRegex = "mymap"

        val notificationDAO = mock<NotificationDAO> {
            onBlocking { find() } doReturn listOf(WC3GameNotification(notificationId, module, Regex(mapRegex)))
        }

        val newGame = mock<Game> {
            on { id } doReturn 1234
            on { map } doReturn mapRegex
        }

        val myModuleNotifier = mock<GameNotifier> {
            onBlocking { closeExpiredGame(newGame) } doThrow RuntimeException("Kaboom!")
        }

        val notifiers = mapOf(module to myModuleNotifier)

        val gameNotificationService = GameNotificationService(notificationDAO, notifiers)

        runBlocking {
            repeat(2) { gameNotificationService.notifyGames(listOf(newGame)) }
            gameNotificationService.notifyGames(emptyList())
        }

        verify(myModuleNotifier) {
            1 * { runBlocking { notifyNewGame(notificationId, newGame) } }
            1 * { runBlocking { updateExistingGame(newGame) } }
            1 * { runBlocking { closeExpiredGame(newGame) } }
        }
    }
}