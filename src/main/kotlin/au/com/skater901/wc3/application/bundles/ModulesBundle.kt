package au.com.skater901.wc3.application.bundles

import au.com.skater901.wc3.WC3NotificationBotConfiguration
import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.application.config.ConfigParser
import au.com.skater901.wc3.application.module.NotificationModulesModule
import au.com.skater901.wc3.core.service.WC3GameNotificationServiceImpl
import au.com.skater901.wc3.utilities.collections.ifNotEmpty
import com.google.inject.*
import com.google.inject.name.Names.named
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.getInstance
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import jakarta.inject.Named
import java.util.*

internal class ModulesBundle(
    private val notificationModules: List<NotificationModule<Any, *, *>>,
    private val injectorProvider: () -> Injector
) : BaseBundle {
    override val module: KotlinModule = NotificationModulesModule(notificationModules)

    lateinit var modulesInjector: Injector
        private set

    override fun initialize(bootstrap: Bootstrap<*>) {
        notificationModules.groupBy { it.moduleName }
            .filter { it.value.size > 1 }
            .map { (moduleName, modules) ->
                "Multiple modules registered with name [ $moduleName ]: ${modules.joinToString { it::class.qualifiedName!! }}."
            }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString(" ")) }
    }

    override fun run(configuration: WC3NotificationBotConfiguration, environment: Environment) {
        notificationModules.filter { it.gameNotifier() == null && it.gameNotifierClass == null }
            .map { "Module [ ${it.moduleName} ] does not provide a GameNotifier." }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString(" ")) }

        notificationModules.filter { it.gameNotifier() != null && it.gameNotifierClass != null }
            .map { "Module [ ${it.moduleName} ] provides both a GameNotifier instance and GameNotifier class. Please provide one or the other, not both." }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString(" ")) }

        notificationModules.filter { it.scheduledTask() != null && it.scheduledTaskClass != null }
            .map { "Module [ ${it.moduleName} ] provides both a scheduled task instance and scheduled task class for dependency injection. Please provide one or the other, not both." }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString(" ")) }

        notificationModules.flatMap {
            val gameNotificationModule = it.makeGuiceModule()

            val childInjector = injectorProvider().createChildInjector(gameNotificationModule, it.guiceModule())

            it.initializeNotificationHandlers(
                childInjector.getInstance(it.configClass.java),
                childInjector,
                childInjector.getInstance<WC3GameNotificationService>()
            )

            listOf(gameNotificationModule, it.guiceModule())
        }
            .also {
                modulesInjector = injectorProvider().createChildInjector(*it.toTypedArray())
            }
    }

    private fun NotificationModule<Any, *, *>.makeGuiceModule(): AbstractModule = object : KotlinModule() {
        override fun configure() {
            bind<WC3GameNotificationService>().to<WC3GameNotificationServiceImpl>()

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
}