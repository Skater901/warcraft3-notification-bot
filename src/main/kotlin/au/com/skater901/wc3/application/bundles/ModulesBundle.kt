package au.com.skater901.wc3.application.bundles

import au.com.skater901.wc3.WC3NotificationBotConfiguration
import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.application.module.NotificationModulesModule
import au.com.skater901.wc3.utilities.collections.ifNotEmpty
import com.google.inject.Injector
import com.google.inject.Key
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment

internal class ModulesBundle(
    private val notificationModules: List<NotificationModule<*>>,
    private val injectorProvider: () -> Injector
) : BaseBundle {
    override val module: KotlinModule = NotificationModulesModule(notificationModules)

    override fun initialize(bootstrap: Bootstrap<*>) {
        notificationModules.groupBy { it.moduleName }
            .filter { it.value.size > 1 }
            .map { (moduleName, modules) ->
                "Multiple modules registered with name [ $moduleName ]: ${modules.joinToString { it::class.qualifiedName!! }}."
            }
            .ifNotEmpty { throw IllegalArgumentException(it.joinToString(" ")) }
    }

    override fun run(configuration: WC3NotificationBotConfiguration, environment: Environment) {
        notificationModules.forEach {
            (it as NotificationModule<Any>).initializeNotificationHandlers(
                injectorProvider().getInstance(it.configClass.java),
                injectorProvider(),
                injectorProvider().getInstance(Key.get(WC3GameNotificationService::class.java, it.annotation.java))
            )
        }
    }
}