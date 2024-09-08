package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.api.NotificationModule
import com.google.inject.AbstractModule
import com.google.inject.Provides
import jakarta.inject.Singleton
import java.util.*

internal class NotificationModulesModule : AbstractModule() {
    @Provides
    @Singleton
    fun getModules(): List<NotificationModule<Any, *, *>> = ServiceLoader.load(NotificationModule::class.java)
        .map { it as NotificationModule<Any, *, *> }
        .let {
            val enabledModules = System.getProperty("enabledModules")
                ?.split(",")
                ?.map { n -> n.trim() }
                ?.toSet()
                ?: return@let it
            it.filter { m -> m.moduleName in enabledModules }
        }
}