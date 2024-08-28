package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.api.NotificationModule
import com.google.inject.AbstractModule
import com.google.inject.Provides
import jakarta.inject.Singleton
import java.util.*

internal class NotificationModulesModule : AbstractModule() {
    @Provides
    @Singleton
    fun getModules(): List<NotificationModule<Any, *, *>> = ServiceLoader.load(NotificationModule::class.java)
        .map { it as NotificationModule<Any, *, *> } // TODO add filtering to only enable specified modules
}