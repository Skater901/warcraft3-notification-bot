package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.service.GameNotifier
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.multibindings.KotlinMapBinder.Companion.newMapBinder

internal class GameNotifierModule(private val modules: List<NotificationModule<*>>) : KotlinModule() {
    override fun configure() {
        val binder = newMapBinder<String, GameNotifier>(binder())

        modules.forEach {
            binder.addBinding(it.moduleName).to(it.gameNotifier.java)
        }
    }
}