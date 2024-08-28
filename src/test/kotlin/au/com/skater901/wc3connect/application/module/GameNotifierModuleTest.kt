package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.api.core.domain.Game
import au.com.skater901.wc3connect.api.core.service.GameNotifier
import com.google.inject.Guice
import com.google.inject.Key
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GameNotifierModuleTest {
    class GameNotifier1 : GameNotifier {
        override suspend fun notifyNewGame(notificationId: String, game: Game) {
        }

        override suspend fun updateExistingGame(game: Game) {
        }

        override suspend fun closeExpiredGame(game: Game) {
        }
    }

    class GameNotifier2 : GameNotifier {
        override suspend fun notifyNewGame(notificationId: String, game: Game) {
        }

        override suspend fun updateExistingGame(game: Game) {
        }

        override suspend fun closeExpiredGame(game: Game) {
        }
    }

    @Test
    fun `should bind game notifiers to module name`() {
        val injector = Guice.createInjector(
            GameNotifierModule(
                mapOf(
                    "module1" to GameNotifier1(),
                    "module2" to GameNotifier2()
                )
            )
        )

        val notifiers = injector.getInstance(object : Key<Map<String, GameNotifier>>() {})

        assertThat(notifiers["module1"]).isInstanceOf(GameNotifier1::class.java)
        assertThat(notifiers["module2"]).isInstanceOf(GameNotifier2::class.java)
    }
}