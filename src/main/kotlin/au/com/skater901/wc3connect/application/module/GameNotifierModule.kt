package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.api.core.service.GameNotifier
import com.google.inject.AbstractModule
import com.google.inject.multibindings.MapBinder.newMapBinder

internal class GameNotifierModule(
    private val modules: List<NotificationModule<*>>
) : AbstractModule() {
    override fun configure() {
        val binder = newMapBinder(binder(), String::class.java, GameNotifier::class.java)

        modules.forEach {
            binder.addBinding(it.moduleName).to(it.gameNotifier.java)
        }
    }
}