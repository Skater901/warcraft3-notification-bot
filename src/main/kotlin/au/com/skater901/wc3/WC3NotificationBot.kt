package au.com.skater901.wc3

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.application.bundles.*
import au.com.skater901.wc3.application.config.ConfigParser
import au.com.skater901.wc3.application.module.AdminModule
import au.com.skater901.wc3.application.module.AppModule
import au.com.skater901.wc3.application.module.ClientModule
import au.com.skater901.wc3.application.provider.WC3GameNotificationServiceProvider
import au.com.skater901.wc3.resources.AdminResource
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.Scopes
import com.google.inject.name.Names.named
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.key
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.github.classgraph.ClassGraph
import ru.vyarus.dropwizard.guice.GuiceBundle
import java.util.*

internal class WC3NotificationBot : Application<WC3NotificationBotConfiguration>() {
    override fun initialize(bootstrap: Bootstrap<WC3NotificationBotConfiguration>) {
        val notificationModules = ServiceLoader.load(NotificationModule::class.java)
            .map { it as NotificationModule<*> }
            .let {
                val enabledModules = System.getProperty("enabledModules")
                    ?.split(",")
                    ?.map { n -> n.trim() }
                    ?.toSet()
                    ?: return@let it
                it.filter { m -> m.moduleName in enabledModules }
            }

        ClassGraph().acceptPackages(
            NotificationModule::class.java.packageName,
            javaClass.packageName,
            *notificationModules.map { it.javaClass.packageName }
                .toTypedArray()
        )
            .enableAnnotationInfo()
            .enableMethodInfo()
            .scan()
            .use { scanResult ->
                lateinit var guiceBundle: GuiceBundle

                val bundles = listOf(
                    ConfigBundle(scanResult, javaClass.classLoader),
                    DatabaseBundle { guiceBundle.injector },
                    ModulesBundle(notificationModules) { guiceBundle.injector },
                    GameNotifierBundle(notificationModules) { guiceBundle.injector },
                    ScheduledTasksBundle(notificationModules) { guiceBundle.injector }
                )

                guiceBundle = GuiceBundle.builder()
                    .modules(
                        AppModule(scanResult),
                        ClientModule(scanResult),
                        AdminModule(notificationModules),
                        *bundles.mapNotNull { it.module }.toTypedArray(),
                        *notificationModules.map { it.guiceModule }.toTypedArray(),
                        *notificationModules.map {
                            object : KotlinModule() {
                                override fun configure() {
                                    val configProperties = getProvider(
                                        key<Properties>()
                                            .withAnnotation(named("configProperties"))
                                    )

                                    bind((it as NotificationModule<Any>).configClass.java).toProvider(
                                        ConfigParser(
                                            configProperties,
                                            it.moduleName,
                                            it.configClass
                                        )
                                    )
                                        .`in`(Scopes.SINGLETON)

                                    bind<WC3GameNotificationService>().annotatedWith(it.annotation.java)
                                        .toProvider(WC3GameNotificationServiceProvider(getProvider(), it.moduleName))
                                }
                            }
                        }
                            .toTypedArray()
                    )
                    .extensions(AdminResource::class.java)
                    .build()

                bootstrap.addBundle(guiceBundle)
                bundles.forEach { bootstrap.addBundle(it) }
            }
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