package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.scheduled.ScheduledTask
import com.google.inject.Injector
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.getInstance
import dev.misfitlabs.kotlinguice4.multibindings.KotlinMultibinder.Companion.newSetBinder

internal class ScheduledTasksModule(private val injectorProvider: () -> Injector) : KotlinModule() {
    override fun configure() {
        val binder = newSetBinder<Pair<ScheduledTask, NotificationModule<Any, *, *>>>(binder())

        injectorProvider().getInstance<List<@JvmSuppressWildcards NotificationModule<Any, *, *>>>()
            .forEach {
                val task = it.scheduledTask()

                if (task != null) {
                    binder.addBinding().toInstance(task to it)
                }

                if (it.scheduledTaskClass != null) {
                    val taskProvider = getProvider(it.scheduledTaskClass!!.java)

                    binder.addBinding().toProvider { taskProvider.get() to it }
                }
            }
    }
}