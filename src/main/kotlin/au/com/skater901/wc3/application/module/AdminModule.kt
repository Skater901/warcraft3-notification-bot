package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.service.AdminMessageNotifier
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.multibindings.KotlinMapBinder.Companion.newMapBinder

internal class AdminModule(private val modules: List<NotificationModule<*>>) : KotlinModule() {
    override fun configure() {
        val binder = newMapBinder<String, AdminMessageNotifier>(binder())

        modules.forEach {
            if (it.adminMessageNotifier != null) {
                binder.addBinding(it.moduleName).to(it.adminMessageNotifier!!.java)
            }
        }
    }
}