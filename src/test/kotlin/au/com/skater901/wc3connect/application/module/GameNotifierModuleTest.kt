package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.api.core.domain.Game
import au.com.skater901.wc3connect.api.core.service.GameNotifier
import au.com.skater901.wc3connect.api.core.service.WC3GameNotificationService
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class GameNotifierModuleTest {
    class GameNotifier1 : GameNotifier {
        override suspend fun notifyNewGame(notificationId: String, game: Game) {
            TODO("Not yet implemented")
        }

        override suspend fun updateExistingGame(game: Game) {
            TODO("Not yet implemented")
        }

        override suspend fun closeExpiredGame(game: Game) {
            TODO("Not yet implemented")
        }
    }

    class GameNotifier2 : GameNotifier {
        override suspend fun notifyNewGame(notificationId: String, game: Game) {
            TODO("Not yet implemented")
        }

        override suspend fun updateExistingGame(game: Game) {
            TODO("Not yet implemented")
        }

        override suspend fun closeExpiredGame(game: Game) {
            TODO("Not yet implemented")
        }
    }

    private val module1 = object : NotificationModule<Any> {
        override val moduleName: String = "module1"
        override val configClass: KClass<Any>
            get() = TODO("Not yet implemented")

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
            TODO("Not yet implemented")
        }

        override val gameNotifier: KClass<out GameNotifier> = GameNotifier1::class
    }

    private val module2 = object : NotificationModule<Any> {
        override val moduleName: String = "module2"
        override val configClass: KClass<Any>
            get() = TODO("Not yet implemented")

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
            TODO("Not yet implemented")
        }

        override val gameNotifier: KClass<out GameNotifier> = GameNotifier2::class
    }

    @Test
    fun `should bind game notifiers to module name`() {
        val injector = Guice.createInjector(GameNotifierModule(listOf(module1, module2)))

        val notifiers = injector.getInstance(object : Key<Map<String, GameNotifier>>() {})

        assertThat(notifiers["module1"]).isInstanceOf(GameNotifier1::class.java)
        assertThat(notifiers["module2"]).isInstanceOf(GameNotifier2::class.java)
    }
}