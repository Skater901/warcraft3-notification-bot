package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.api.core.service.GameNotifier
import com.google.inject.AbstractModule
import com.google.inject.multibindings.MapBinder.newMapBinder

internal class GameNotifierModule(
    private val gameNotifiers: Map<String, GameNotifier>
) : AbstractModule() {
    override fun configure() {
        val binder = newMapBinder(binder(), String::class.java, GameNotifier::class.java)

        gameNotifiers.forEach { (moduleName, gameNotifier) ->
            binder.addBinding(moduleName).toInstance(gameNotifier)
        }
    }
}