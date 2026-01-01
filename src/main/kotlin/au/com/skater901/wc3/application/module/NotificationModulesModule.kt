package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.api.NotificationModule
import com.google.inject.Provides
import dev.misfitlabs.kotlinguice4.KotlinModule

internal class NotificationModulesModule(
    private val notificationModules: List<NotificationModule<*>>
) : KotlinModule() {
    @Provides
    fun getModules(): List<NotificationModule<*>> = notificationModules
}