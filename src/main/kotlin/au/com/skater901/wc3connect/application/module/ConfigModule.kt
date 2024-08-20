package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.application.config.ConfigParser
import au.com.skater901.wc3connect.application.config.DatabaseConfig
import au.com.skater901.wc3connect.application.config.GamesConfiguration
import au.com.skater901.wc3connect.application.config.LogConfiguration
import au.com.skater901.wc3connect.application.logging.LoggingConfiguration
import com.google.inject.AbstractModule
import com.google.inject.Key
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.internal.SingletonScope
import com.google.inject.name.Names.named
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.io.File
import java.util.*

internal class ConfigModule(
    private val modules: List<NotificationModule<Any>>
) : AbstractModule() {
    override fun configure() {
        val configProperties = getProvider(Key.get(Properties::class.java).withAnnotation(named("configProperties")))

        bind(DatabaseConfig::class.java).toProvider(
            ConfigParser(
                configProperties,
                "database",
                DatabaseConfig::class
            )
        )
            .`in`(Scopes.SINGLETON)
        bind(GamesConfiguration::class.java).toProvider(
            ConfigParser(
                configProperties,
                "gamesConfiguration",
                GamesConfiguration::class
            )
        )
            .`in`(Scopes.SINGLETON)
        bind(LogConfiguration::class.java).toProvider(
            ConfigParser(
                configProperties,
                "logging",
                LogConfiguration::class
            )
        )
            .`in`(Scopes.SINGLETON)

        modules.forEach {
            bind(it.configClass.java).toProvider(
                ConfigParser(
                    configProperties,
                    it.moduleName,
                    it.configClass
                )
            )
                .`in`(Scopes.SINGLETON)
        }

        requestStaticInjection(LoggingConfiguration::class.java)
    }

    @Provides
    @Singleton
    @Named("configProperties")
    fun provideConfig(): Properties = Properties().apply {
        File(System.getProperty("configFile")).inputStream().use { load(it) }
    }
}