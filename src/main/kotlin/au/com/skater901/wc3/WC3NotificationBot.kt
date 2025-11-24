package au.com.skater901.wc3

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.application.bundles.*
import au.com.skater901.wc3.application.module.AppModule
import au.com.skater901.wc3.application.module.ClientModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.github.classgraph.ClassGraph
import ru.vyarus.dropwizard.guice.GuiceBundle
import java.util.*

internal class WC3NotificationBot : Application<WC3NotificationBotConfiguration>() {
    private lateinit var guiceBundle: GuiceBundle
    private lateinit var modulesBundle: ModulesBundle

    override fun initialize(bootstrap: Bootstrap<WC3NotificationBotConfiguration>) {
        val notificationModules = ServiceLoader.load(NotificationModule::class.java)
            .map { it as NotificationModule<Any, *, *> }
            .let {
                val enabledModules = System.getProperty("enabledModules")
                    ?.split(",")
                    ?.map { n -> n.trim() }
                    ?.toSet()
                    ?: return@let it
                it.filter { m -> m.moduleName in enabledModules }
            }

        modulesBundle = ModulesBundle(notificationModules) { guiceBundle.injector }

        val bundles: List<BaseBundle>

        guiceBundle = ClassGraph().acceptPackages(
            NotificationModule::class.java.packageName,
            javaClass.packageName,
            *notificationModules.map { it.javaClass.packageName }
                .toTypedArray()
        )
            .enableAnnotationInfo()
            .enableMethodInfo()
            .scan()
            .use { scanResult ->
                bundles = listOf(
                    ConfigBundle(scanResult, javaClass.classLoader),
                    DatabaseBundle { guiceBundle.injector },
                    modulesBundle,
                    GameNotifierBundle { modulesBundle.modulesInjector },
                    ScheduledTasksBundle { modulesBundle.modulesInjector }
                )
                GuiceBundle.builder()
                    .modules(
                        AppModule(scanResult),
                        ClientModule(scanResult)
                    )
                    .modules(
                        *bundles.mapNotNull { it.module }
                            .toTypedArray()
                    )
                    .build()
            }

        bootstrap.addBundle(guiceBundle)
        bundles.forEach { bootstrap.addBundle(it) }
    }

    override fun run(configuration: WC3NotificationBotConfiguration, environment: Environment) {
        environment.objectMapper
            .registerModule(JavaTimeModule())
            .registerKotlinModule()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            WC3NotificationBot().run(*args)
        }
    }
}