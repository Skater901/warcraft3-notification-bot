package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.service.GameNotifier
import com.google.inject.Injector
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.getInstance
import dev.misfitlabs.kotlinguice4.multibindings.KotlinMapBinder.Companion.newMapBinder

internal class GameNotifierModule(private val injectorProvider: () -> Injector) : KotlinModule() {
    override fun configure() {
        val binder = newMapBinder<String, GameNotifier>(binder())

        injectorProvider().getInstance<List<@JvmSuppressWildcards NotificationModule<Any, *, *>>>()
            .forEach {
                binder.addBinding(it.moduleName)
                    .run {
                        val notifier = it.gameNotifier()

                        if (notifier != null)
                            toInstance(notifier)
                        else
                            toProvider(getProvider(it.gameNotifierClass!!.java))
                    }
            }
    }
}