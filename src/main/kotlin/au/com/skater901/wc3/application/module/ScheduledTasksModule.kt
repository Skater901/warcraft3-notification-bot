package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.scheduled.ScheduledTask
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.multibindings.KotlinMultibinder.Companion.newSetBinder

internal class ScheduledTasksModule(private val modules: List<NotificationModule<*>>) : KotlinModule() {
    override fun configure() {
        val binder = newSetBinder<Pair<ScheduledTask, NotificationModule<*>>>(binder())

        modules.forEach {
            if (it.scheduledTask != null) {
                val taskProvider = getProvider(it.scheduledTask!!.java)

                binder.addBinding().toProvider { taskProvider.get() to it }
            }
        }
    }
}