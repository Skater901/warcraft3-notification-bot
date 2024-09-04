package au.com.skater901.wc3.application.managed

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.service.GameNotifier
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.application.config.ConfigParser
import au.com.skater901.wc3.core.job.TaskRunner
import au.com.skater901.wc3.core.service.WC3GameNotificationServiceImpl
import au.com.skater901.wc3.utilities.collections.ifNotEmpty
import au.com.skater901.wc3.utils.getInstance
import com.google.inject.*
import com.google.inject.name.Names.named
import jakarta.inject.Inject
import jakarta.inject.Named
import java.util.*

internal class ModuleManager @Inject constructor(
    private val notificationModules: List<@JvmSuppressWildcards NotificationModule<Any, *, *>>,
    private val taskRunner: TaskRunner
) {
    private var started = false
    private val gameNotifiers = mutableMapOf<String, GameNotifier>()

    fun initializeModules(injector: Injector) {
        if (started) return

        validateModules()

        notificationModules.forEach {
            val gameNotificationModule = it.makeGuiceModule()

            val childInjector = injector.createChildInjector(gameNotificationModule, it.guiceModule())

            it.initializeNotificationHandlers(
                childInjector.getInstance(it.configClass.java),
                childInjector,
                childInjector.getInstance<WC3GameNotificationService>()
            )

            if (it.scheduledTask() != null || it.scheduledTaskClass != null) {
                taskRunner.runTask(it, injector)
            }

            gameNotifiers[it.moduleName] = (it.gameNotifierClass
                ?.let { gameNotifierClass -> childInjector.getInstance(gameNotifierClass.java) }
                ?: it.gameNotifier()!!)
        }

        started = true
    }

    private fun validateModules() {
        notificationModules.groupBy { it.moduleName }
            .filter { it.value.size > 1 }
            .map { (moduleName, modules) ->
                "Multiple modules registered with name [ $moduleName ]: ${modules.joinToString { it::class.qualifiedName!! }}."
            }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString(" ")) }

        notificationModules.filter { it.gameNotifier() == null && it.gameNotifierClass == null }
            .map { "Module [ ${it.moduleName} ] does not provide a GameNotifier." }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString(" ")) }

        notificationModules.filter { it.gameNotifier() != null && it.gameNotifierClass != null }
            .map { "Module [ ${it.moduleName} ] provides both a GameNotifier instance and GameNotifier class. Please provide one or the other, not both." }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString(" ")) }

        notificationModules.filter { it.scheduledTask() != null && it.scheduledTaskClass != null }
            .map { "Module [ ${it.moduleName} ] provides both a scheduled task instance and scheduled task class for dependency injection. Please provide one or the other, not both." }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString(" ")) }
    }

    private fun NotificationModule<Any, *, *>.makeGuiceModule(): AbstractModule = object : AbstractModule() {
        override fun configure() {
            bind(WC3GameNotificationService::class.java).to(WC3GameNotificationServiceImpl::class.java)

            val configProperties = getProvider(
                Key.get(Properties::class.java)
                    .withAnnotation(named("configProperties"))
            )

            bind(configClass.java).toProvider(
                ConfigParser(
                    configProperties,
                    moduleName,
                    configClass
                )
            )
                .`in`(Scopes.SINGLETON)
        }

        @Provides
        @Named("moduleName")
        fun getModuleName(): String = moduleName
    }

    fun getGameNotifiers(): Map<String, GameNotifier> = gameNotifiers
}