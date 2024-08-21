package au.com.skater901.wc3connect.application.managed

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3connect.core.job.TaskRunner
import au.com.skater901.wc3connect.core.service.WC3GameNotificationServiceImpl
import au.com.skater901.wc3connect.utilities.collections.ifNotEmpty
import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import jakarta.inject.Named
import java.util.*

// TODO refactor to use injection so it can be tested
internal class ModuleManager {
    val notificationModules = loadModules()
    private var started = false

    init {
        notificationModules.groupBy { it.moduleName }
            .filter { it.value.size > 1 }
            .map { (moduleName, modules) ->
                "Multiple modules registered with name [ $moduleName ]: ${modules.joinToString { it::class.qualifiedName!! }}"
            }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString()) }
    }

    fun initializeModules(injector: Injector) {
        if (started) return

        notificationModules.forEach {
            val gameNotificationModule = object : AbstractModule() {
                override fun configure() {
                    bind(WC3GameNotificationService::class.java).to(WC3GameNotificationServiceImpl::class.java)
                }

                @Provides
                @Named("moduleName")
                fun getModuleName(): String = it.moduleName
            }

            val childInjector = injector.createChildInjector(gameNotificationModule)

            it.initializeNotificationHandlers(
                childInjector.getInstance(it.configClass.java),
                childInjector,
                childInjector.getInstance(WC3GameNotificationService::class.java)
            )
        }

        notificationModules.filter { it.scheduledTask != null }
            .ifNotEmpty { TaskRunner(it.toList()).start() }

        started = true
    }

    private fun loadModules(): List<NotificationModule<Any>> = ServiceLoader.load(NotificationModule::class.java)
        .map { it as NotificationModule<Any> }
}